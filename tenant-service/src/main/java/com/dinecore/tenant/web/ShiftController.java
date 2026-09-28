package com.dinecore.tenant.web;

import com.dinecore.tenant.api.ShiftResponse;
import com.dinecore.tenant.security.CurrentUser;
import com.dinecore.tenant.service.ShiftService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {

    private final ShiftService shifts;

    public ShiftController(ShiftService shifts) {
        this.shifts = shifts;
    }

    @PostMapping("/open")
    public ShiftResponse open() {
        return shifts.open(CurrentUser.get());
    }

    @PostMapping("/close")
    public ShiftResponse close(@RequestBody(required = false) CloseShiftRequest request) {
        UUID shiftId = request == null ? null : request.shiftId();
        return shifts.close(CurrentUser.get(), shiftId);
    }

    public record CloseShiftRequest(UUID shiftId) {
    }
}
