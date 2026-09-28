package com.dinecore.order.security;

import java.security.Principal;

public record SocketUser(String userId, String tenantId) implements Principal {

    @Override
    public String getName() {
        return userId;
    }
}
