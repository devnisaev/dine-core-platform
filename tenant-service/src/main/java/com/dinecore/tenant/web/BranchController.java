package com.dinecore.tenant.web;

import com.dinecore.tenant.api.BranchResponse;
import com.dinecore.tenant.security.CurrentUser;
import com.dinecore.tenant.service.BranchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
public class BranchController {

    private final BranchService branches;

    public BranchController(BranchService branches) {
        this.branches = branches;
    }

    @PostMapping
    public BranchResponse create(@RequestBody CreateBranchRequest request) {
        return branches.create(request.organizationId(), request.name(), request.code(), request.currency(), request.timezone());
    }

    @GetMapping
    public List<BranchResponse> list() {
        return branches.list(CurrentUser.get());
    }

    public record CreateBranchRequest(UUID organizationId, String name, String code, String currency, String timezone) {
    }
}
