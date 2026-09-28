package com.dinecore.order.event;

import com.dinecore.order.error.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutionException;

@Component
@ConditionalOnProperty(name = "dinecore.orders.bus", havingValue = "kafka")
public class KafkaOrderEvents implements OrderEvents {

    static final String TOPIC = "dinecore.orders";

    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;

    public KafkaOrderEvents(KafkaTemplate<String, String> kafka, JsonMapper json) {
        this.kafka = kafka;
        this.json = json;
    }

    @Override
    public void publish(Envelope event) {
        try {
            kafka.send(TOPIC, event.tenantId().toString(), json.writeValueAsString(event)).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw failed();
        } catch (ExecutionException ex) {
            throw failed();
        }
    }

    private static ApiException failed() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PUBLISH_FAILED", "Order publish failed");
    }
}
