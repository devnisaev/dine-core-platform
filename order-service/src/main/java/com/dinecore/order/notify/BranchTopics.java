package com.dinecore.order.notify;

public interface BranchTopics {

    void send(String destination, Object body);
}
