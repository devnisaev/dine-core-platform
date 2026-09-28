package com.dinecore.tenant.service;

import com.dinecore.common.Role;

import java.util.UUID;

public record CreateUser(String username, String password, Role role, UUID organizationId) {
}
