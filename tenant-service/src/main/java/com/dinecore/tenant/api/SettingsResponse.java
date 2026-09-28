package com.dinecore.tenant.api;

import com.dinecore.tenant.domain.BranchSettings;

import java.math.BigDecimal;

public record SettingsResponse(BigDecimal taxRate, BigDecimal serviceChargePct, String receiptFooterText) {

    public static SettingsResponse from(BranchSettings settings) {
        return new SettingsResponse(settings.getTaxRate(), settings.getServiceChargePct(), settings.getReceiptFooterText());
    }
}
