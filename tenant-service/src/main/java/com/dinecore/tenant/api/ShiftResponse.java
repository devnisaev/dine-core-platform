package com.dinecore.tenant.api;

import com.dinecore.tenant.domain.Shift;

import java.time.Instant;
import java.util.UUID;

public record ShiftResponse(UUID id, UUID branchId, UUID userId, Instant openedAt, Instant closedAt) {

    public static ShiftResponse from(Shift shift) {
        return new ShiftResponse(shift.getId(), shift.getBranchId(), shift.getUserId(), shift.getOpenedAt(), shift.getClosedAt());
    }
}
