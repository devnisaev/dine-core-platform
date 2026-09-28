package com.dinecore.menu.event;

import java.time.Instant;
import java.util.UUID;

public record MenuChanged(UUID eventId, String eventType, Instant occurredAt, UUID organizationId, UUID tenantId, UUID branchId) {

    public static MenuChanged branch(UUID organizationId, UUID branchId) {
        return new MenuChanged(UUID.randomUUID(), "MenuChanged", Instant.now(), organizationId, branchId, branchId);
    }

    public static MenuChanged catalog(UUID organizationId) {
        return new MenuChanged(UUID.randomUUID(), "MenuChanged", Instant.now(), organizationId, null, null);
    }
}
