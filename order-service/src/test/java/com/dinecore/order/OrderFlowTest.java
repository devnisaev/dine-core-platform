package com.dinecore.order;

import com.dinecore.common.Role;
import com.dinecore.order.domain.OrderStatus;
import com.dinecore.order.error.ApiException;
import com.dinecore.order.event.MemoryOrderEvents;
import com.dinecore.order.event.OrderPayloads;
import com.dinecore.order.menu.StubMenuClient;
import com.dinecore.order.notify.Destinations;
import com.dinecore.order.notify.RecordingBranchTopics;
import com.dinecore.order.repo.DiningOrderRepository;
import com.dinecore.order.repo.OrderItemRepository;
import com.dinecore.order.service.Caller;
import com.dinecore.order.service.CancelService;
import com.dinecore.order.service.KitchenService;
import com.dinecore.order.service.OrderLookup;
import com.dinecore.order.service.OrderService;
import com.dinecore.order.service.SubmitService;
import com.dinecore.order.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderFlowTest {

    @Autowired
    private TableService tables;

    @Autowired
    private OrderService orders;

    @Autowired
    private SubmitService submit;

    @Autowired
    private KitchenService kitchen;

    @Autowired
    private CancelService cancel;

    @Autowired
    private StubMenuClient menu;

    @Autowired
    private MemoryOrderEvents events;

    @Autowired
    private RecordingBranchTopics topics;

    @Autowired
    private DiningOrderRepository orderRepository;

    @Autowired
    private OrderItemRepository items;

    @Autowired
    private OrderLookup lookup;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID branchA = UUID.randomUUID();
    private final UUID branchB = UUID.randomUUID();
    private final Caller waiter = new Caller(UUID.randomUUID(), Role.WAITER, organizationId);
    private final Caller admin = new Caller(UUID.randomUUID(), Role.BRANCH_ADMIN, organizationId);
    private final Caller cook = new Caller(UUID.randomUUID(), Role.KITCHEN, organizationId);

    @BeforeEach
    void reset() {
        events.clear();
        topics.clear();
    }

    @Test
    void emptySubmitPublishesNothing() {
        UUID orderId = draft(branchA);
        events.clear();
        topics.clear();

        ApiException error = org.junit.jupiter.api.Assertions.assertThrows(ApiException.class,
                () -> submit.submit(waiter, branchA, orderId, "Bearer token"));
        org.junit.jupiter.api.Assertions.assertEquals("ORDER_STATE", error.code());
        assertThat(events.published()).isEmpty();
        assertThat(topics.sent()).isEmpty();
    }

    @Test
    void submitKeepsThePriceAndNotifiesThatBranchKitchen() {
        UUID dishId = UUID.randomUUID();
        menu.set(dishId, "Lagman", new BigDecimal("450.00"));
        UUID orderId = draft(branchA);
        UUID itemId = orders.addItem(waiter, branchA, orderId, dishId, 1, java.util.List.of()).items().get(0).id();
        events.clear();
        topics.clear();

        submit.submit(waiter, branchA, orderId, "Bearer token");
        menu.set(dishId, "Lagman", new BigDecimal("480.00"));

        assertThat(items.findByOrderId(orderId).get(0).getUnitPrice()).isEqualByComparingTo("450.00");
        assertThat(events.published()).anyMatch(event -> "OrderSubmitted".equals(event.eventType()));
        assertThat(topics.sent()).anyMatch(delivery -> delivery.destination().equals(Destinations.kitchen(branchA)));
        assertThat(topics.sent()).noneMatch(delivery -> delivery.destination().contains(branchB.toString()));
        assertThatThrownBy(() -> lookup.order(branchB, orderId)).isInstanceOf(ApiException.class);

        kitchen.ready(cook, branchA, orderId, itemId);
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.READY);
        assertThat(events.published()).anyMatch(event -> "OrderReady".equals(event.eventType())
                && event.payload() instanceof OrderPayloads.Ready);
    }

    @Test
    void failedPublishLeavesTheOrderDraft() {
        UUID dishId = UUID.randomUUID();
        menu.set(dishId, "Lagman", new BigDecimal("450.00"));
        UUID orderId = draft(branchA);
        orders.addItem(waiter, branchA, orderId, dishId, 1, java.util.List.of());
        events.failNext();

        ApiException error = org.junit.jupiter.api.Assertions.assertThrows(ApiException.class,
                () -> submit.submit(waiter, branchA, orderId, "Bearer token"));
        org.junit.jupiter.api.Assertions.assertEquals("PUBLISH_FAILED", error.code());
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(items.findByOrderId(orderId).get(0).getUnitPrice()).isNull();
        assertThat(events.published()).isEmpty();
    }

    @Test
    void waiterCannotCancelAfterSubmit() {
        UUID dishId = UUID.randomUUID();
        menu.set(dishId, "Lagman", new BigDecimal("450.00"));
        UUID orderId = draft(branchA);
        orders.addItem(waiter, branchA, orderId, dishId, 1, java.util.List.of());
        submit.submit(waiter, branchA, orderId, "Bearer token");
        events.clear();

        ApiException error = org.junit.jupiter.api.Assertions.assertThrows(ApiException.class,
                () -> cancel.cancel(waiter, branchA, orderId, "changed mind"));
        org.junit.jupiter.api.Assertions.assertEquals("FORBIDDEN", error.code());
        cancel.cancel(admin, branchA, orderId, "86");
        assertThat(events.published()).anyMatch(event -> "OrderCancelled".equals(event.eventType()));
        assertThat(topics.sent()).anyMatch(delivery -> delivery.destination().equals(Destinations.kitchen(branchA)));
    }

    private UUID draft(UUID branchId) {
        UUID tableId = tables.create(branchId, number(), 4).id();
        return orders.open(waiter, branchId, tableId, null).id();
    }

    private static int number() {
        return (int) (Math.random() * 100000) + 1;
    }
}
