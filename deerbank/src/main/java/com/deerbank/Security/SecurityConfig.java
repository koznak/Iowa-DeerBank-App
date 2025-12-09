package com.deerbank.Security;

import com.deerbank.Security.ApiKeyAuthenticationProvider;
import com.deerbank.Security.ApiKeyAuthFilter;
import com.deerbank.Security.ApiKeyProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Imports for CORS
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiKeyProperties apiKeyProperties;

    public SecurityConfig( ApiKeyProperties apiKeyProperties) {
//        this.jwtFilter = jwtFilter;
        this.apiKeyProperties = apiKeyProperties;
    }

    // 1. Define the AuthenticationManager Bean using our custom Provider
    @Bean
    public AuthenticationManager authenticationManager() {
        // NOTE: Ensure apiKeyProperties.getKey() matches the actual getter in ApiKeyProperties
        return new ProviderManager(
                Collections.singletonList(new ApiKeyAuthenticationProvider(apiKeyProperties.getKey()))
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allows ALL origins (the wildcard "*")
        configuration.setAllowedOrigins(Collections.singletonList("*"));

        // Allows all common methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allows all headers (CRITICAL: Ensures X-API-Key is passed)
        configuration.setAllowedHeaders(Collections.singletonList("*"));

        // Must be false when using "*" for origins
        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L); // 1 hour

        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 2. Configure the Security Filter Chain, injecting the AuthenticationManager
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager, JwtFilter jwtFilter) throws Exception {

        // 1. Apply the CORS Configuration using the bean defined above
        http.cors(Customizer.withDefaults())

                // 2. Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Set Session Policy to Stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // 4. Register the custom API Key Filter
        http
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                new ApiKeyAuthFilter("X-API-Key", authenticationManager),
                UsernamePasswordAuthenticationFilter.class
        );

        // 5. Authorize Requests: Require authentication for all endpoints
        http.authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
        );

        return http.build();
    }

    @Bean
    public JwtFilter jwtFilter(JwtService jwtService) {
        return new JwtFilter(jwtService);
    }


}