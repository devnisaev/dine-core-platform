package com.dinecore.order.security;

import com.dinecore.common.JwtClaims;
import com.dinecore.common.Role;
import com.dinecore.order.error.ApiException;
import com.dinecore.order.service.Caller;
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
        return caller(jwt());
    }

    private static Caller caller(Jwt jwt) {
        return new Caller(UUID.fromString(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString(JwtClaims.ROLE)),
                UUID.fromString(jwt.getClaimAsString(JwtClaims.ORGANIZATION_ID)));
    }

    private static Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH", "Authentication required");
    }
}
