package com.dinecore.order.service;

import com.dinecore.common.Role;
import com.dinecore.order.api.OrderResponse;
import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.DiningTable;
import com.dinecore.order.domain.OrderItem;
import com.dinecore.order.domain.OrderStatus;
import com.dinecore.order.error.ApiException;
import com.dinecore.order.notify.BranchTopics;
import com.dinecore.order.notify.Destinations;
import com.dinecore.order.repo.DiningOrderRepository;
import com.dinecore.order.repo.OrderItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderLookup lookup;
    private final DiningOrderRepository orders;
    private final OrderItemRepository items;
    private final BranchTopics topics;

    public OrderService(OrderLookup lookup, DiningOrderRepository orders, OrderItemRepository items, BranchTopics topics) {
        this.lookup = lookup;
        this.orders = orders;
        this.items = items;
        this.topics = topics;
    }

    @Transactional
    public OrderResponse open(Caller caller, UUID branchId, UUID tableId, String note) {
        if (caller.role() != Role.WAITER) {
            throw Guard.forbidden();
        }
        DiningTable table = lookup.table(branchId, tableId);
        Guard.available(table.getStatus());
        table.occupy();
        DiningOrder order = orders.save(new DiningOrder(UUID.randomUUID(), branchId, tableId, caller.userId(), Checks.note(note)));
        topics.send(Destinations.tables(branchId), com.dinecore.order.api.TableResponse.from(table));
        return OrderResponse.from(order, List.of());
    }

    @Transactional
    public OrderResponse addItem(Caller caller, UUID branchId, UUID orderId, UUID dishId, int quantity, List<UUID> modifierIds) {
        DiningOrder order = lookup.order(branchId, orderId);
        Guard.waiterOwns(caller, order);
        Guard.draft(order);
        if (dishId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Dish is required");
        }
        items.save(new OrderItem(UUID.randomUUID(), orderId, dishId, Checks.positive(quantity, "Quantity"),
                Modifiers.write(modifierIds)));
        return OrderResponse.from(order, items.findByOrderId(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(Caller caller, UUID branchId, String status) {
        return load(branchId, status).stream().filter(order -> visible(caller, order)).map(this::view).toList();
    }

    private List<DiningOrder> load(UUID branchId, String status) {
        if (status == null || status.isBlank()) {
            return orders.findByBranchId(branchId);
        }
        try {
            return orders.findByBranchIdAndStatus(branchId, OrderStatus.valueOf(status));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Unknown order status");
        }
    }

    private boolean visible(Caller caller, DiningOrder order) {
        if (caller.role() == Role.WAITER) {
            return caller.userId().equals(order.getWaiterId());
        }
        if (caller.role() == Role.KITCHEN) {
            return order.getStatus() == OrderStatus.SENT_TO_KITCHEN || order.getStatus() == OrderStatus.READY;
        }
        return true;
    }

    private OrderResponse view(DiningOrder order) {
        return OrderResponse.from(order, items.findByOrderId(order.getId()));
    }
}
