package com.dinecore.tenant.service;

import com.dinecore.tenant.api.LoginResponse;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.domain.UserAccount;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwords;
    private final Tokens tokens;

    public AuthService(UserAccountRepository users, PasswordEncoder passwords, Tokens tokens) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    public LoginResponse login(String username, String password) {
        UserAccount user = users.findByUsername(username == null ? "" : username.trim())
                .filter(found -> found.getStatus() == Status.ACTIVE)
                .filter(found -> passwords.matches(password == null ? "" : password, found.getPasswordHash()))
                .orElseThrow(AuthService::invalid);
        return new LoginResponse(tokens.issue(user), tokens.ttlSeconds(), user.getRole(), user.getOrganizationId(), user.getBranchId());
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH", "Invalid credentials");
    }
}
