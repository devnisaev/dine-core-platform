package com.dinecore.order.service;

import com.dinecore.order.api.OrderResponse;
import com.dinecore.order.api.TableResponse;
import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.DiningTable;
import com.dinecore.order.domain.OrderItem;
import com.dinecore.order.domain.OrderStatus;
import com.dinecore.order.event.Envelope;
import com.dinecore.order.event.OrderEvents;
import com.dinecore.order.event.OrderPayloads;
import com.dinecore.order.notify.BranchTopics;
import com.dinecore.order.notify.Destinations;
import com.dinecore.order.repo.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CancelService {

    private final OrderLookup lookup;
    private final OrderItemRepository items;
    private final OrderEvents events;
    private final BranchTopics topics;

    public CancelService(OrderLookup lookup, OrderItemRepository items, OrderEvents events, BranchTopics topics) {
        this.lookup = lookup;
        this.items = items;
        this.events = events;
        this.topics = topics;
    }

    @Transactional
    public OrderResponse cancel(Caller caller, UUID branchId, UUID orderId, String reason) {
        DiningOrder order = lookup.order(branchId, orderId);
        boolean afterSubmit = afterSubmit(caller, order);
        List<OrderItem> lines = items.findByOrderId(orderId);
        lines.forEach(OrderItem::cancel);
        order.cancel();
        DiningTable table = lookup.table(branchId, order.getTableId());
        table.available();
        notify(caller, branchId, order, afterSubmit, reason, table);
        return OrderResponse.from(order, lines);
    }

    private void notify(Caller caller, UUID branchId, DiningOrder order, boolean afterSubmit, String reason, DiningTable table) {
        if (afterSubmit) {
            OrderPayloads.Cancelled payload = new OrderPayloads.Cancelled(order.getId(), Checks.reason(reason));
            events.publish(Envelope.of("OrderCancelled", caller.organizationId(), branchId, payload));
            topics.send(Destinations.kitchen(branchId), payload);
        }
        topics.send(Destinations.tables(branchId), TableResponse.from(table));
    }

    private static boolean afterSubmit(Caller caller, DiningOrder order) {
        if (order.getStatus() == OrderStatus.DRAFT) {
            Guard.draftCancel(caller, order);
            return false;
        }
        if (order.getStatus() == OrderStatus.SUBMITTED || order.getStatus() == OrderStatus.SENT_TO_KITCHEN) {
            Guard.kitchenCancel(caller);
            return true;
        }
        throw Guard.state("Order cannot be cancelled");
    }
}
