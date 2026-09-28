package com.dinecore.gateway;

import com.dinecore.common.RequestHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import java.util.UUID;

@Configuration
public class RequestIdConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    WebFilter requestIdFilter() {
        return (exchange, chain) -> chain.filter(withRequestId(exchange));
    }

    private static ServerWebExchange withRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(RequestHeaders.REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        ServerHttpRequest request = exchange.getRequest().mutate().header(RequestHeaders.REQUEST_ID, requestId).build();
        exchange.getResponse().getHeaders().set(RequestHeaders.REQUEST_ID, requestId);
        return exchange.mutate().request(request).build();
    }
}
