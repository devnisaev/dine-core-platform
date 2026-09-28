package com.dinecore.tenant.service;

import com.dinecore.common.HmacKeys;
import com.dinecore.common.JwtClaims;
import com.dinecore.common.Role;
import com.dinecore.tenant.domain.Status;
import com.dinecore.tenant.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TokensTest {

    private static final String SECRET = "dinecore-dev-secret-change-me-32b";

    @Test
    void superAdminTokenOmitsTheTenantClaim() {
        Tokens tokens = new Tokens(NimbusJwtEncoder.withSecretKey(HmacKeys.fromUtf8(SECRET)).build(), 3600);
        UserAccount admin = new UserAccount(UUID.randomUUID(), UUID.randomUUID(), null, "super", "hash", Role.SUPER_ADMIN, Status.ACTIVE);
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(HmacKeys.fromUtf8(SECRET)).build();

        var jwt = decoder.decode(tokens.issue(admin));
        assertEquals("SUPER_ADMIN", jwt.getClaimAsString(JwtClaims.ROLE));
        assertFalse(jwt.getClaims().containsKey(JwtClaims.TENANT_ID));
    }
}
