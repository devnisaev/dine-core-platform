package com.dinecore.order.service;

import com.dinecore.order.api.OrderResponse;
import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.OrderItem;
import com.dinecore.order.domain.OrderStatus;
import com.dinecore.order.event.Envelope;
import com.dinecore.order.event.OrderEvents;
import com.dinecore.order.event.OrderPayloads;
import com.dinecore.order.menu.MenuClient;
import com.dinecore.order.menu.PricedDish;
import com.dinecore.order.notify.BranchTopics;
import com.dinecore.order.notify.Destinations;
import com.dinecore.order.repo.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubmitService {

    private final OrderLookup lookup;
    private final OrderItemRepository items;
    private final MenuClient menu;
    private final OrderEvents events;
    private final BranchTopics topics;

    public SubmitService(OrderLookup lookup, OrderItemRepository items, MenuClient menu, OrderEvents events, BranchTopics topics) {
        this.lookup = lookup;
        this.items = items;
        this.menu = menu;
        this.events = events;
        this.topics = topics;
    }

    @Transactional
    public OrderResponse submit(Caller caller, UUID branchId, UUID orderId, String authorization) {
        DiningOrder order = lookup.order(branchId, orderId);
        Guard.waiterOwns(caller, order);
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw Guard.state("Order cannot be submitted");
        }
        List<OrderItem> lines = items.findByOrderId(order.getId());
        if (lines.isEmpty()) {
            throw Guard.state("Cannot submit an empty order");
        }
        snapshot(branchId, authorization, lines);
        order.sentToKitchen();
        publish(caller, branchId, order, lines);
        return OrderResponse.from(order, lines);
    }

    private void snapshot(UUID branchId, String authorization, List<OrderItem> lines) {
        for (OrderItem item : lines) {
            PricedDish priced = menu.price(branchId, item.getDishId(), Modifiers.readIds(item.getModifiersJson()), authorization);
            item.snapshot(priced.name(), priced.unitPrice(), priced.modifiersJson());
        }
    }

    private void publish(Caller caller, UUID branchId, DiningOrder order, List<OrderItem> lines) {
        int tableNumber = lookup.table(branchId, order.getTableId()).getTableNumber();
        OrderPayloads.Submitted payload = OrderPayloads.submitted(order, tableNumber, lines);
        events.publish(Envelope.of("OrderSubmitted", caller.organizationId(), branchId, payload));
        topics.send(Destinations.kitchen(branchId), payload);
    }
}
