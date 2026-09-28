package com.dinecore.tenant.api;

import com.dinecore.tenant.domain.Branch;
import com.dinecore.tenant.domain.Status;

import java.util.UUID;

public record BranchResponse(UUID id, UUID organizationId, String name, String code, String currency, String timezone, Status status) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getOrganizationId(), branch.getName(), branch.getCode(),
                branch.getCurrency(), branch.getTimezone(), branch.getStatus());
    }
}
