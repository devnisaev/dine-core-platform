package com.dinecore.gateway;

import com.dinecore.common.HmacKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

@Configuration
public class JwtConfig {

    @Bean
    JwtDecoder jwtDecoder(@Value("${dinecore.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(HmacKeys.fromUtf8(secret)).build();
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(JwtDecoder jwtDecoder) {
        return token -> Mono.fromCallable(() -> jwtDecoder.decode(token));
    }
}
