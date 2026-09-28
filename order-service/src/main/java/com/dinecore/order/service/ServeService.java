package com.dinecore.order.service;

import com.dinecore.order.api.OrderResponse;
import com.dinecore.order.domain.DiningOrder;
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
public class ServeService {

    private final OrderLookup lookup;
    private final OrderItemRepository items;
    private final OrderEvents events;
    private final BranchTopics topics;

    public ServeService(OrderLookup lookup, OrderItemRepository items, OrderEvents events, BranchTopics topics) {
        this.lookup = lookup;
        this.items = items;
        this.events = events;
        this.topics = topics;
    }

    @Transactional
    public OrderResponse served(Caller caller, UUID branchId, UUID orderId) {
        DiningOrder order = lookup.order(branchId, orderId);
        Guard.waiterOwns(caller, order);
        if (order.getStatus() != OrderStatus.READY) {
            throw Guard.state("Order is not ready to serve");
        }
        order.served();
        List<OrderItem> lines = items.findByOrderId(orderId);
        OrderPayloads.Served payload = OrderPayloads.served(order, lines);
        events.publish(Envelope.of("OrderServed", caller.organizationId(), branchId, payload));
        topics.send(Destinations.waiter(branchId, order.getWaiterId()), payload);
        return OrderResponse.from(order, lines);
    }
}
