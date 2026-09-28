package com.dinecore.tenant.api;

import com.dinecore.common.Role;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.domain.UserAccount;

import java.util.UUID;

public record UserResponse(UUID id, String username, Role role, UUID organizationId, UUID branchId, Status status) {

    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getOrganizationId(), user.getBranchId(), user.getStatus());
    }
}
