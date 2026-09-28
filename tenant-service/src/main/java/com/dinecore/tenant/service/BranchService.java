package com.dinecore.tenant.service;

import com.dinecore.common.Role;
import com.dinecore.tenant.api.BranchResponse;
import com.dinecore.tenant.domain.Branch;
import com.dinecore.tenant.domain.BranchSettings;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.BranchRepository;
import com.dinecore.tenant.repo.BranchSettingsRepository;
import com.dinecore.tenant.repo.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BranchService {

    private final BranchRepository branches;
    private final BranchSettingsRepository settings;
    private final OrganizationRepository organizations;

    public BranchService(BranchRepository branches, BranchSettingsRepository settings, OrganizationRepository organizations) {
        this.branches = branches;
        this.settings = settings;
        this.organizations = organizations;
    }

    @Transactional
    public BranchResponse create(UUID organizationId, String name, String code, String currency, String timezone) {
        requireOrganization(organizationId);
        String requiredCode = Texts.required(code, "Code is required");
        if (branches.existsByCode(requiredCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE", "Branch code already exists");
        }
        Branch saved = branches.save(newBranch(organizationId, name, requiredCode, currency, timezone));
        settings.save(new BranchSettings(saved.getId(), BigDecimal.ZERO.setScale(4), BigDecimal.ZERO.setScale(4), ""));
        return BranchResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> list(Caller caller) {
        if (caller.role() == Role.SUPER_ADMIN) {
            return branches.findAll().stream().map(BranchResponse::from).toList();
        }
        return branches.findByOrganizationId(caller.organizationId()).stream().map(BranchResponse::from).toList();
    }

    private void requireOrganization(UUID organizationId) {
        if (organizationId == null || !organizations.existsById(organizationId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Organization not found");
        }
    }

    private static Branch newBranch(UUID organizationId, String name, String code, String currency, String timezone) {
        return new Branch(UUID.randomUUID(), organizationId, Texts.required(name, "Name is required"), code,
                Texts.required(currency, "Currency is required"), Texts.required(timezone, "Timezone is required"), Status.ACTIVE);
    }
}
