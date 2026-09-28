package com.dinecore.order.notify;

public final class TopicAccess {

    private TopicAccess() {
    }

    public static boolean allows(String destination, String tenantId) {
        if (destination == null || tenantId == null || tenantId.isBlank()) {
            return false;
        }
        return destination.startsWith("/topic/branch/" + tenantId + "/");
    }
}
