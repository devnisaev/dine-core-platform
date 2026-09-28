package com.dinecore.menu.security;

import com.dinecore.common.JwtClaims;
import com.dinecore.menu.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID organizationId() {
        String value = jwt().getClaimAsString(JwtClaims.ORGANIZATION_ID);
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH", "Authentication required");
        }
        return UUID.fromString(value);
    }

    private static Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH", "Authentication required");
    }
}
