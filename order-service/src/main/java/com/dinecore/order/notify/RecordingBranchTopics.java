package com.dinecore.order.notify;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "dinecore.orders.topics", havingValue = "memory")
public class RecordingBranchTopics implements BranchTopics {

    private final List<Delivery> sent = new ArrayList<>();

    @Override
    public void send(String destination, Object body) {
        sent.add(new Delivery(destination, body));
    }

    public List<Delivery> sent() {
        return List.copyOf(sent);
    }

    public void clear() {
        sent.clear();
    }

    public record Delivery(String destination, Object body) {
    }
}
