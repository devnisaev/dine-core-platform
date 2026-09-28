package com.dinecore.tenant.api;

import com.dinecore.tenant.domain.Organization;
import com.dinecore.tenant.domain.Status;

import java.util.UUID;

public record OrganizationResponse(UUID id, String name, String code, Status status) {

    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(organization.getId(), organization.getName(), organization.getCode(), organization.getStatus());
    }
}
