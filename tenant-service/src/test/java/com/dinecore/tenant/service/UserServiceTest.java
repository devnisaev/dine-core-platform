package com.dinecore.tenant.service;

import com.dinecore.common.Role;
import com.dinecore.tenant.domain.Branch;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.BranchRepository;
import com.dinecore.tenant.repo.UserAccountRepository;
import com.dinecore.tenant.web.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final BranchRepository branches = mock(BranchRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final UserService service = new UserService(users, branches, passwords);

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void branchAdminCannotCreateASuperAdmin() {
        Caller admin = new Caller(UUID.randomUUID(), Role.BRANCH_ADMIN, UUID.randomUUID());
        CreateUser command = new CreateUser("new-admin", "password", Role.SUPER_ADMIN, admin.organizationId());
        assertThrows(ApiException.class, () -> service.create(command, admin));
    }

    @Test
    void branchAdminCreatesAWaiterOnTheCurrentBranch() {
        UUID branchId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        TenantContext.set(branchId.toString());
        when(branches.findById(branchId)).thenReturn(Optional.of(branch(branchId, organizationId)));
        when(passwords.encode("password")).thenReturn("hash");
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Caller admin = new Caller(UUID.randomUUID(), Role.BRANCH_ADMIN, organizationId);

        var created = service.create(new CreateUser("new-waiter", "password", Role.WAITER, null), admin);
        assertEquals(branchId, created.branchId());
        assertEquals(Role.WAITER, created.role());
    }

    private static Branch branch(UUID id, UUID organizationId) {
        return new Branch(id, organizationId, "Bishkek 01", "bishkek-01", "KGS", "Asia/Bishkek", Status.ACTIVE);
    }
}
