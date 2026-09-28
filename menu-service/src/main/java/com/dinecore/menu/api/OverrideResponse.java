package com.dinecore.menu.api;

import com.dinecore.menu.domain.BranchMenuOverride;

import java.math.BigDecimal;
import java.util.UUID;

public record OverrideResponse(UUID branchId, UUID dishId, BigDecimal customPrice, boolean available) {

    public static OverrideResponse from(BranchMenuOverride override) {
        return new OverrideResponse(override.getId().getBranchId(), override.getId().getDishId(),
                override.getCustomPrice(), override.isAvailable());
    }
}
