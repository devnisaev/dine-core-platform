package com.dinecore.tenant.security;

import com.dinecore.common.HmacKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Bean
    JwtDecoder jwtDecoder(@Value("${dinecore.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(key(secret)).build();
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${dinecore.jwt.secret}") String secret) {
        return NimbusJwtEncoder.withSecretKey(key(secret)).build();
    }

    private static SecretKey key(String secret) {
        return HmacKeys.fromUtf8(secret);
    }
}
