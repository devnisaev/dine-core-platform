package com.dinecore.tenant.service;

import com.dinecore.tenant.domain.BranchSettings;
import com.dinecore.tenant.repo.BranchSettingsRepository;
import com.dinecore.tenant.web.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettingsServiceTest {

    private final BranchSettingsRepository settings = mock(BranchSettingsRepository.class);
    private final SettingsService service = new SettingsService(settings);

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void returnsOnlyTheBranchInTheRequestContext() {
        UUID branchA = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID branchB = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(settings.findById(branchB)).thenReturn(Optional.of(new BranchSettings(branchB, new BigDecimal("0.0800"), new BigDecimal("0.1000"), "b")));
        TenantContext.set(branchB.toString());

        assertEquals(new BigDecimal("0.0800"), service.get().taxRate());
        verify(settings).findById(branchB);
        org.mockito.Mockito.verify(settings, org.mockito.Mockito.never()).findById(branchA);
    }
}
