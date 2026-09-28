package com.dinecore.menu.security;

import com.dinecore.common.AccessVerdict;
import com.dinecore.common.ApiAccess;
import com.dinecore.common.JwtClaims;
import com.dinecore.common.RequestHeaders;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;

public class AccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        AccessVerdict verdict = ApiAccess.decide(request.getMethod(), path(request), role(), tenantClaim(), header(request));
        if (verdict.allowed()) {
            chain.doFilter(request, response);
            return;
        }
        int status = verdict.decision() == AccessVerdict.Decision.UNAUTHORIZED ? 401 : 403;
        HttpErrors.write(response, status, verdict.code(), verdict.message(), request);
    }

    private static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            return uri.substring(context.length());
        }
        return uri;
    }

    private static String header(HttpServletRequest request) {
        return request.getHeader(RequestHeaders.TENANT_ID);
    }

    private static String role() {
        Jwt jwt = jwt();
        return jwt == null ? null : jwt.getClaimAsString(JwtClaims.ROLE);
    }

    private static String tenantClaim() {
        Jwt jwt = jwt();
        return jwt == null ? null : jwt.getClaimAsString(JwtClaims.TENANT_ID);
    }

    private static Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        return null;
    }
}
