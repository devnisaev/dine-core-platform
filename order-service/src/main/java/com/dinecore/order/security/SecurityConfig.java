package com.dinecore.order.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    AccessFilter accessFilter() {
        return new AccessFilter();
    }

    @Bean
    FilterRegistrationBean<AccessFilter> accessFilterRegistration(AccessFilter filter) {
        FilterRegistrationBean<AccessFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AccessFilter accessFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/ws", "/ws/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(SecurityConfig::unauthorized)
                        .accessDeniedHandler(SecurityConfig::forbidden))
                .addFilterAfter(accessFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    private static void unauthorized(HttpServletRequest request, HttpServletResponse response, Exception ex) throws java.io.IOException {
        HttpErrors.write(response, 401, "AUTH", "Authentication required", request);
    }

    private static void forbidden(HttpServletRequest request, HttpServletResponse response, Exception ex) throws java.io.IOException {
        HttpErrors.write(response, 403, "FORBIDDEN", "Not allowed", request);
    }
}
