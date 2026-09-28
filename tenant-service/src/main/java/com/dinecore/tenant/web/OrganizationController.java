package com.dinecore.tenant.web;

import com.dinecore.tenant.api.OrganizationResponse;
import com.dinecore.tenant.service.OrganizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizations;

    public OrganizationController(OrganizationService organizations) {
        this.organizations = organizations;
    }

    @PostMapping
    public OrganizationResponse create(@RequestBody CreateOrganizationRequest request) {
        return organizations.create(request.name(), request.code());
    }

    @GetMapping
    public List<OrganizationResponse> list() {
        return organizations.list();
    }

    public record CreateOrganizationRequest(String name, String code) {
    }
}
