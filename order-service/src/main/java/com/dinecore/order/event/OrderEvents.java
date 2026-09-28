package com.dinecore.order.event;

public interface OrderEvents {

    void publish(Envelope event);
}
