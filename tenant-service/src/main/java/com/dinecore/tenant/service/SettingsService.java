package com.dinecore.tenant.service;

import com.dinecore.tenant.api.SettingsResponse;
import com.dinecore.tenant.domain.BranchSettings;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.BranchSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class SettingsService {

    private final BranchSettingsRepository settings;

    public SettingsService(BranchSettingsRepository settings) {
        this.settings = settings;
    }

    @Transactional(readOnly = true)
    public SettingsResponse get() {
        return SettingsResponse.from(load(TenantIds.current()));
    }

    @Transactional
    public SettingsResponse update(BigDecimal taxRate, BigDecimal serviceChargePct, String footer) {
        BranchSettings current = load(TenantIds.current());
        current.update(rate(taxRate, "Tax rate"), rate(serviceChargePct, "Service charge"), footer(footer));
        return SettingsResponse.from(settings.save(current));
    }

    private BranchSettings load(UUID branchId) {
        return settings.findById(branchId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Settings not found"));
    }

    private static BigDecimal rate(BigDecimal value, String name) {
        if (value == null || value.signum() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", name + " must be zero or positive");
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private static String footer(String value) {
        String text = value == null ? "" : value;
        if (text.length() > 500) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Receipt footer is too long");
        }
        return text;
    }
}
