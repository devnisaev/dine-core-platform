package com.dinecore.gateway;

import com.dinecore.common.AccessVerdict;
import com.dinecore.common.JwtClaims;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessWebFilterTest {

    @Test
    void waiterOfBranchAIsRejectedForBranchB() {
        String branchA = "11111111-1111-1111-1111-111111111111";
        String branchB = "22222222-2222-2222-2222-222222222222";
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(UUID.randomUUID().toString())
                .claim(JwtClaims.ROLE, "WAITER")
                .claim(JwtClaims.TENANT_ID, branchA)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        AccessVerdict verdict = AccessWebFilter.verdict("GET", "/api/v1/settings", new JwtAuthenticationToken(jwt), branchB);
        assertEquals(AccessVerdict.Decision.FORBIDDEN, verdict.decision());
        assertEquals("TENANT_MISMATCH", verdict.code());
    }
}
