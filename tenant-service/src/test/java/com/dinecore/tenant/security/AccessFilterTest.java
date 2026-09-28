package com.dinecore.tenant.security;

import com.dinecore.common.JwtClaims;
import com.dinecore.common.RequestHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AccessFilterTest {

    private final AccessFilter filter = new AccessFilter();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void waiterCannotCreateAnOrganization() throws Exception {
        authenticate("WAITER", branchA());
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/api/v1/organizations", branchA()), response, chain());
        assertEquals(403, response.getStatus());
        assertFalse(response.getContentAsString().isEmpty());
    }

    @Test
    void waiterOfBranchAIsRejectedForBranchB() throws Exception {
        authenticate("WAITER", branchA());
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request("GET", "/api/v1/settings", branchB()), response, chain());
        assertEquals(403, response.getStatus());
        assertEquals(true, response.getContentAsString().contains("TENANT_MISMATCH"));
    }

    private static MockHttpServletRequest request(String method, String path, String tenant) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader(RequestHeaders.TENANT_ID, tenant);
        return request;
    }

    private static void authenticate(String role, String tenant) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(UUID.randomUUID().toString())
                .claim(JwtClaims.ROLE, role)
                .claim(JwtClaims.ORGANIZATION_ID, UUID.randomUUID().toString())
                .claim(JwtClaims.TENANT_ID, tenant)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private static FilterChain chain() {
        return (request, response) -> ((HttpServletResponse) response).setStatus(200);
    }

    private static String branchA() {
        return "11111111-1111-1111-1111-111111111111";
    }

    private static String branchB() {
        return "22222222-2222-2222-2222-222222222222";
    }
}
