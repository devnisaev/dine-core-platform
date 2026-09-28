package com.dinecore.order.service;

import com.dinecore.common.Role;

import java.util.UUID;

public record Caller(UUID userId, Role role, UUID organizationId) {
}
