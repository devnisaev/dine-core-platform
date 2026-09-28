package com.dinecore.tenant.service;

import com.dinecore.common.Role;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.domain.UserAccount;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.repo.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final Tokens tokens = mock(Tokens.class);
    private final AuthService service = new AuthService(users, passwords, tokens);

    @Test
    void loginReturnsTheIssuedToken() {
        UserAccount user = user();
        when(users.findByUsername("waiter")).thenReturn(Optional.of(user));
        when(passwords.matches("password", "hash")).thenReturn(true);
        when(tokens.issue(user)).thenReturn("jwt");
        when(tokens.ttlSeconds()).thenReturn(3600L);

        assertEquals("jwt", service.login("waiter", "password").accessToken());
        assertEquals(Role.WAITER, service.login("waiter", "password").role());
    }

    @Test
    void wrongPasswordIsUnauthorized() {
        when(users.findByUsername("waiter")).thenReturn(Optional.of(user()));
        when(passwords.matches("nope", "hash")).thenReturn(false);
        assertThrows(ApiException.class, () -> service.login("waiter", "nope"));
    }

    private static UserAccount user() {
        return new UserAccount(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "waiter", "hash", Role.WAITER, Status.ACTIVE);
    }
}
