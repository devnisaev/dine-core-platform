package com.dinecore.tenant.security;

import com.dinecore.common.JwtClaims;
import com.dinecore.common.Role;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.service.Caller;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Caller get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH", "Authentication required");
        }
        return caller(token.getToken());
    }

    private static Caller caller(Jwt jwt) {
        return new Caller(UUID.fromString(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString(JwtClaims.ROLE)),
                UUID.fromString(jwt.getClaimAsString(JwtClaims.ORGANIZATION_ID)));
    }
}
