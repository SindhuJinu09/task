package com.algobrewery.apikeymgmt.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CSRF protection
            .csrf(csrf -> csrf.enable())
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/client/**").permitAll()
                .requestMatchers("/").permitAll() // Allow ALB health check
                .anyRequest().authenticated()
            )
            
            // Security headers
            .headers(headers -> headers
                .frameOptions().deny() // Prevent clickjacking
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                    .preload(true)
                )
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=()")
                    .policy("midi=()")
                    .policy("sync-xhr=()")
                    .policy("microphone=()")
                    .policy("camera=()")
                    .policy("magnetometer=()")
                    .policy("gyroscope=()")
                    .policy("fullscreen=(self)")
                    .policy("payment=()")
                )
            )
            
            // Add custom API key filter
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
} 