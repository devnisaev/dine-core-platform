package com.dinecore.order.event;

import com.dinecore.order.error.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "dinecore.orders.bus", havingValue = "memory")
public class MemoryOrderEvents implements OrderEvents {

    private final List<Envelope> published = new ArrayList<>();
    private boolean fail;

    @Override
    public void publish(Envelope event) {
        if (fail) {
            fail = false;
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PUBLISH_FAILED", "Order publish failed");
        }
        published.add(event);
    }

    public void failNext() {
        this.fail = true;
    }

    public List<Envelope> published() {
        return List.copyOf(published);
    }

    public void clear() {
        published.clear();
        fail = false;
    }
}
