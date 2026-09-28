package com.dinecore.tenant.service;

import com.dinecore.common.Role;
import com.dinecore.tenant.api.ShiftResponse;
import com.dinecore.tenant.domain.Shift;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ShiftService {

    private final ShiftRepository shifts;

    public ShiftService(ShiftRepository shifts) {
        this.shifts = shifts;
    }

    @Transactional
    public ShiftResponse open(Caller caller) {
        if (shifts.findByUserIdAndClosedAtIsNull(caller.userId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE", "Shift is already open");
        }
        Shift shift = new Shift(UUID.randomUUID(), TenantIds.current(), caller.userId(), Instant.now(), null);
        return ShiftResponse.from(shifts.save(shift));
    }

    @Transactional
    public ShiftResponse close(Caller caller, UUID shiftId) {
        Shift shift = shiftId == null ? ownOpen(caller.userId()) : existing(shiftId);
        assertCanClose(caller, shift);
        shift.close(Instant.now());
        return ShiftResponse.from(shifts.save(shift));
    }

    private void assertCanClose(Caller caller, Shift shift) {
        if (shift.getClosedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE", "Shift is already closed");
        }
        boolean owner = caller.userId().equals(shift.getUserId());
        if (!owner && caller.role() != Role.BRANCH_ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not allowed");
        }
        if (!shift.getBranchId().equals(TenantIds.current())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "TENANT_MISMATCH", "Tenant header does not match the token");
        }
    }

    private Shift ownOpen(UUID userId) {
        return shifts.findByUserIdAndClosedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Open shift not found"));
    }

    private Shift existing(UUID shiftId) {
        return shifts.findById(shiftId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Shift not found"));
    }
}
