package com.dinecore.menu.security;

import com.dinecore.common.HmacKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Bean
    JwtDecoder jwtDecoder(@Value("${dinecore.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(key(secret)).build();
    }

    private static SecretKey key(String secret) {
        return HmacKeys.fromUtf8(secret);
    }
}
