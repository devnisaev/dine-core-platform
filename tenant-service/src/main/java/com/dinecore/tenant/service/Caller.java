package com.dinecore.tenant.service;

import com.dinecore.common.Role;

import java.util.UUID;

public record Caller(UUID userId, Role role, UUID organizationId) {
}
