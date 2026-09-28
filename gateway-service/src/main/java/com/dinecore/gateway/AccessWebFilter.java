package com.dinecore.gateway;

import com.dinecore.common.ApiAccess;
import com.dinecore.common.AccessVerdict;
import com.dinecore.common.JwtClaims;
import com.dinecore.common.RequestHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class AccessWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> verdict(exchange, authentication))
                .defaultIfEmpty(verdict(exchange, null))
                .flatMap(verdict -> apply(exchange, chain, verdict));
    }

    static AccessVerdict verdict(ServerWebExchange exchange, Authentication authentication) {
        return verdict(exchange.getRequest().getMethod().name(), path(exchange), authentication, header(exchange));
    }

    static AccessVerdict verdict(String method, String path, Authentication authentication, String header) {
        if (!(authentication instanceof JwtAuthenticationToken token)) {
            return ApiAccess.decide(method, path, null, null, header);
        }
        Jwt jwt = token.getToken();
        return ApiAccess.decide(method, path, jwt.getClaimAsString(JwtClaims.ROLE), jwt.getClaimAsString(JwtClaims.TENANT_ID), header);
    }

    private Mono<Void> apply(ServerWebExchange exchange, WebFilterChain chain, AccessVerdict verdict) {
        if (verdict.allowed()) {
            return chain.filter(exchange);
        }
        int status = verdict.decision() == AccessVerdict.Decision.UNAUTHORIZED ? 401 : 403;
        return write(exchange, status, verdict.code(), verdict.message());
    }

    private static Mono<Void> write(ServerWebExchange exchange, int status, String code, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(status));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = new com.dinecore.common.ApiError(code, message, traceId(exchange)).json().getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private static String path(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().pathWithinApplication().value();
    }

    private static String header(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(RequestHeaders.TENANT_ID);
    }

    private static String traceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(RequestHeaders.REQUEST_ID);
        if (traceId == null || traceId.isBlank()) {
            return java.util.UUID.randomUUID().toString();
        }
        return traceId;
    }
}
