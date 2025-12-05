package com.deerbank.Security;
// ApiKeyAuthenticationProvider.java

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

    private final String validApiKey;

    public ApiKeyAuthenticationProvider(String validApiKey) {
        this.validApiKey = validApiKey;
    }

    /**
     * Required method 1: Performs the actual API Key validation.
     * @throws AuthenticationException if validation fails.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 1. Cast and get the API key from the unauthenticated object
        String apiKey = (String) authentication.getPrincipal();

        if (this.validApiKey == null) {
            throw new BadCredentialsException("Server Configuration Error: API Key not loaded.");
        }

        if (validApiKey.equals(apiKey)) {
            // 2. Key matches: return a fully authenticated token
            return new ApiKeyAuthentication(apiKey, true);
        } else {
            // 3. Key does not match: throw an exception
            throw new BadCredentialsException("API Key validation failed.");
        }
    }

    /**
     * Required method 2: Tells the ProviderManager that this provider
     * can handle ApiKeyAuthentication tokens.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        // This provider only supports ApiKeyAuthentication objects
        return ApiKeyAuthentication.class.isAssignableFrom(authentication);
    }
}