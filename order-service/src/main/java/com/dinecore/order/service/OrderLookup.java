package com.dinecore.order.service;

import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.DiningTable;
import com.dinecore.order.domain.OrderItem;
import com.dinecore.order.repo.DiningOrderRepository;
import com.dinecore.order.repo.DiningTableRepository;
import com.dinecore.order.repo.OrderItemRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderLookup {

    private final DiningOrderRepository orders;
    private final DiningTableRepository tables;
    private final OrderItemRepository items;

    public OrderLookup(DiningOrderRepository orders, DiningTableRepository tables, OrderItemRepository items) {
        this.orders = orders;
        this.tables = tables;
        this.items = items;
    }

    public DiningOrder order(UUID branchId, UUID orderId) {
        DiningOrder found = orders.findById(orderId).orElseThrow(() -> Guard.missing("Order not found"));
        if (!branchId.equals(found.getBranchId())) {
            throw Guard.missing("Order not found");
        }
        return found;
    }

    public DiningTable table(UUID branchId, UUID tableId) {
        DiningTable found = tables.findById(tableId).orElseThrow(() -> Guard.missing("Table not found"));
        if (!branchId.equals(found.getBranchId())) {
            throw Guard.missing("Table not found");
        }
        return found;
    }

    public OrderItem item(UUID orderId, UUID itemId) {
        OrderItem found = items.findById(itemId).orElseThrow(() -> Guard.missing("Item not found"));
        if (!orderId.equals(found.getOrderId())) {
            throw Guard.missing("Item not found");
        }
        return found;
    }
}
