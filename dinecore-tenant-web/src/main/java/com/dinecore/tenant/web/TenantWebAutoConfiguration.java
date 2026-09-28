package com.dinecore.tenant.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@AutoConfiguration
public class TenantWebAutoConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    TenantContextFilter tenantContextFilter() {
        return new TenantContextFilter();
    }
}
