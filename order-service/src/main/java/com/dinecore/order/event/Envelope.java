package com.dinecore.order.event;

import java.time.Instant;
import java.util.UUID;

public record Envelope(UUID eventId, String eventType, Instant occurredAt, UUID organizationId, UUID tenantId, Object payload) {

    public static Envelope of(String eventType, UUID organizationId, UUID tenantId, Object payload) {
        return new Envelope(UUID.randomUUID(), eventType, Instant.now(), organizationId, tenantId, payload);
    }
}
