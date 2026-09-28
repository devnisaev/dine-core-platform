package com.dinecore.tenant.seed;

import com.dinecore.common.Role;
import com.dinecore.tenant.domain.Branch;
import com.dinecore.tenant.domain.BranchSettings;
import com.dinecore.tenant.domain.Organization;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.domain.UserAccount;
import com.dinecore.tenant.repo.BranchRepository;
import com.dinecore.tenant.repo.BranchSettingsRepository;
import com.dinecore.tenant.repo.OrganizationRepository;
import com.dinecore.tenant.repo.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("dev")
public class DevSeed implements CommandLineRunner {

    private static final String PASSWORD = "password";

    private final OrganizationRepository organizations;
    private final BranchRepository branches;
    private final BranchSettingsRepository settings;
    private final UserAccountRepository users;
    private final PasswordEncoder passwords;

    public DevSeed(OrganizationRepository organizations, BranchRepository branches, BranchSettingsRepository settings,
                   UserAccountRepository users, PasswordEncoder passwords) {
        this.organizations = organizations;
        this.branches = branches;
        this.settings = settings;
        this.users = users;
        this.passwords = passwords;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        Organization org = organizations.save(new Organization(UUID.randomUUID(), "Bishkek Group", "bishkek-group", Status.ACTIVE));
        Branch first = saveBranch(org.getId(), "Bishkek 01", "bishkek-01", "0.1200");
        saveBranch(org.getId(), "Bishkek 02", "bishkek-02", "0.0800");
        saveUsers(org.getId(), first.getId());
    }

    private Branch saveBranch(UUID organizationId, String name, String code, String tax) {
        Branch branch = branches.save(new Branch(UUID.randomUUID(), organizationId, name, code, "KGS", "Asia/Bishkek", Status.ACTIVE));
        settings.save(new BranchSettings(branch.getId(), new BigDecimal(tax), new BigDecimal("0.1000"), code));
        return branch;
    }

    private void saveUsers(UUID organizationId, UUID branchId) {
        users.save(user("super", Role.SUPER_ADMIN, organizationId, null));
        users.save(user("admin", Role.BRANCH_ADMIN, organizationId, branchId));
        users.save(user("waiter", Role.WAITER, organizationId, branchId));
        users.save(user("kitchen", Role.KITCHEN, organizationId, branchId));
    }

    private UserAccount user(String username, Role role, UUID organizationId, UUID branchId) {
        return new UserAccount(UUID.randomUUID(), organizationId, branchId, username, passwords.encode(PASSWORD), role, Status.ACTIVE);
    }
}
