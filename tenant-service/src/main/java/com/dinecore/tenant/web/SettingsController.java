package com.dinecore.tenant.web;

import com.dinecore.tenant.api.SettingsResponse;
import com.dinecore.tenant.service.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settings;

    public SettingsController(SettingsService settings) {
        this.settings = settings;
    }

    @GetMapping
    public SettingsResponse get() {
        return settings.get();
    }

    @PutMapping
    public SettingsResponse update(@RequestBody UpdateSettingsRequest request) {
        return settings.update(request.taxRate(), request.serviceChargePct(), request.receiptFooterText());
    }

    public record UpdateSettingsRequest(BigDecimal taxRate, BigDecimal serviceChargePct, String receiptFooterText) {
    }
}
