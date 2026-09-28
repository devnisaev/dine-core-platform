package com.dinecore.order.menu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class MenuClientConfig {

    @Bean
    @ConditionalOnProperty(name = "dinecore.orders.menu", havingValue = "http")
    MenuClient menuHttpClient(RestClient.Builder builder, JsonMapper json, @Value("${dinecore.orders.menu-url}") String baseUrl) {
        return new MenuHttpClient(builder.baseUrl(baseUrl).build(), json);
    }
}
