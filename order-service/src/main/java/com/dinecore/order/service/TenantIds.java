package com.dinecore.order.service;

import com.dinecore.order.error.ApiException;
import com.dinecore.tenant.web.TenantContext;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public final class TenantIds {

    private TenantIds() {
    }

    public static UUID current() {
        String value = TenantContext.get();
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "TENANT_MISMATCH", "Tenant header is required");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Tenant header must be a UUID");
        }
    }
}
