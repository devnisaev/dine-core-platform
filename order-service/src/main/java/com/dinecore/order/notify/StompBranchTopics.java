package com.dinecore.order.notify;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "dinecore.orders.topics", havingValue = "stomp")
public class StompBranchTopics implements BranchTopics {

    private final SimpMessagingTemplate template;

    public StompBranchTopics(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Override
    public void send(String destination, Object body) {
        template.convertAndSend(destination, body);
    }
}
