package com.dinecore.tenant.service;

import com.dinecore.common.JwtClaims;
import com.dinecore.tenant.domain.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class Tokens {

    private final JwtEncoder encoder;
    private final long ttlSeconds;

    public Tokens(JwtEncoder encoder, @Value("${dinecore.jwt.ttl-seconds:3600}") long ttlSeconds) {
        this.encoder = encoder;
        this.ttlSeconds = ttlSeconds;
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }

    public String issue(UserAccount user) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .claim(JwtClaims.ROLE, user.getRole().name())
                .claim(JwtClaims.ORGANIZATION_ID, user.getOrganizationId().toString());
        if (user.getBranchId() != null) {
            claims.claim(JwtClaims.TENANT_ID, user.getBranchId().toString());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
