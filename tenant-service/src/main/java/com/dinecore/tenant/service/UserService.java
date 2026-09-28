package com.dinecore.tenant.service;

import com.dinecore.common.Role;
import com.dinecore.tenant.api.UserResponse;
import com.dinecore.tenant.domain.Branch;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.domain.UserAccount;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.BranchRepository;
import com.dinecore.tenant.repo.UserAccountRepository;
import com.dinecore.tenant.web.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserAccountRepository users;
    private final BranchRepository branches;
    private final PasswordEncoder passwords;

    public UserService(UserAccountRepository users, BranchRepository branches, PasswordEncoder passwords) {
        this.users = users;
        this.branches = branches;
        this.passwords = passwords;
    }

    @Transactional
    public UserResponse create(CreateUser command, Caller caller) {
        String username = Texts.required(command.username(), "Username is required");
        requirePassword(command.password());
        if (users.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE", "Username already exists");
        }
        UserAccount user = caller.role() == Role.SUPER_ADMIN
                ? superAdminUser(command, username)
                : staffUser(command, username, caller);
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(Caller caller) {
        if (caller.role() == Role.SUPER_ADMIN && TenantContext.get() == null) {
            return users.findAll().stream().map(UserResponse::from).toList();
        }
        return users.findByBranchId(TenantIds.current()).stream().map(UserResponse::from).toList();
    }

    private UserAccount superAdminUser(CreateUser command, String username) {
        if (command.role() == Role.SUPER_ADMIN) {
            return account(username, command, requiredOrg(command.organizationId()), null);
        }
        Branch branch = branch(TenantIds.current());
        return account(username, command, branch.getOrganizationId(), branch.getId());
    }

    private UserAccount staffUser(CreateUser command, String username, Caller caller) {
        if (command.role() != Role.WAITER && command.role() != Role.KITCHEN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not allowed");
        }
        Branch branch = branch(TenantIds.current());
        if (!branch.getOrganizationId().equals(caller.organizationId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "TENANT_MISMATCH", "Tenant header does not match the token");
        }
        return account(username, command, branch.getOrganizationId(), branch.getId());
    }

    private UserAccount account(String username, CreateUser command, UUID organizationId, UUID branchId) {
        return new UserAccount(UUID.randomUUID(), organizationId, branchId, username,
                passwords.encode(command.password()), command.role(), Status.ACTIVE);
    }

    private Branch branch(UUID branchId) {
        return branches.findById(branchId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Branch not found"));
    }

    private static UUID requiredOrg(UUID organizationId) {
        if (organizationId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Organization is required");
        }
        return organizationId;
    }

    private static void requirePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Password must be at least 8 characters");
        }
    }
}
