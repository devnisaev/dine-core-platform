package com.dinecore.tenant.service;

import com.dinecore.tenant.api.OrganizationResponse;
import com.dinecore.tenant.domain.Organization;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.OrganizationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizations;

    public OrganizationService(OrganizationRepository organizations) {
        this.organizations = organizations;
    }

    @Transactional
    public OrganizationResponse create(String name, String code) {
        String requiredName = Texts.required(name, "Name is required");
        String requiredCode = Texts.required(code, "Code is required");
        if (organizations.existsByCode(requiredCode)) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE", "Organization code already exists");
        }
        Organization saved = organizations.save(new Organization(UUID.randomUUID(), requiredName, requiredCode, Status.ACTIVE));
        return OrganizationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> list() {
        return organizations.findAll().stream().map(OrganizationResponse::from).toList();
    }
}
