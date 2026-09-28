package com.dinecore.tenant.api;

import com.dinecore.common.Role;
import com.dinecore.tenant.domain.Status;

import java.util.UUID;

public record LoginResponse(String accessToken, long expiresIn, Role role, UUID organizationId, UUID tenantId) {
}
