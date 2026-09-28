package com.dinecore.order.service;

import com.dinecore.order.api.OrderResponse;
import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.ItemStatus;
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
public class KitchenService {

    private final OrderLookup lookup;
    private final OrderItemRepository items;
    private final OrderEvents events;
    private final BranchTopics topics;

    public KitchenService(OrderLookup lookup, OrderItemRepository items, OrderEvents events, BranchTopics topics) {
        this.lookup = lookup;
        this.items = items;
        this.events = events;
        this.topics = topics;
    }

    @Transactional
    public OrderResponse ready(Caller caller, UUID branchId, UUID orderId, UUID itemId) {
        DiningOrder order = lookup.order(branchId, orderId);
        if (order.getStatus() != OrderStatus.SENT_TO_KITCHEN) {
            throw Guard.state("Order is not in the kitchen");
        }
        OrderItem item = lookup.item(orderId, itemId);
        if (item.getStatus() != ItemStatus.QUEUED) {
            throw Guard.state("Item is not queued");
        }
        item.markReady();
        finish(caller, branchId, order);
        return OrderResponse.from(order, items.findByOrderId(orderId));
    }

    private void finish(Caller caller, UUID branchId, DiningOrder order) {
        List<OrderItem> lines = items.findByOrderId(order.getId());
        if (lines.stream().allMatch(item -> item.getStatus() == ItemStatus.READY)) {
            order.ready();
            OrderPayloads.Ready payload = new OrderPayloads.Ready(order.getId(), order.getWaiterId());
            events.publish(Envelope.of("OrderReady", caller.organizationId(), branchId, payload));
            topics.send(Destinations.waiter(branchId, order.getWaiterId()), payload);
        }
        topics.send(Destinations.kitchen(branchId), new OrderPayloads.Ready(order.getId(), order.getWaiterId()));
    }
}
