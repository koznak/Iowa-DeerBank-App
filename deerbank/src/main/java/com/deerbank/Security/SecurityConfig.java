package com.deerbank.Security;

import com.deerbank.Security.ApiKeyAuthenticationProvider;
import com.deerbank.Security.ApiKeyAuthFilter;
import com.deerbank.Security.ApiKeyProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiKeyProperties apiKeyProperties;

    public SecurityConfig(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    // 1. Define the AuthenticationManager Bean using our custom Provider
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(
                Collections.singletonList(new ApiKeyAuthenticationProvider(apiKeyProperties.getKey()))
        );
    }

    // 2. Configure the Security Filter Chain, injecting the AuthenticationManager
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Line 48: Add our custom filter, passing BOTH required arguments
        http.addFilterBefore(
                new ApiKeyAuthFilter("X-API-Key", authenticationManager), // <-- FIX APPLIED
                UsernamePasswordAuthenticationFilter.class
        );

        // Authorize requests: All requests must be authenticated
        http.authorizeHttpRequests(authorize -> authorize
                // .requestMatchers("/public/**").permitAll() // Example public path
                .anyRequest().authenticated()
        );

        return http.build();
    }
}
