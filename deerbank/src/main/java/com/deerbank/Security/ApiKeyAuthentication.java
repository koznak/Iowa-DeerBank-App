package com.deerbank.Security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

// ApiKeyAuthentication.java
public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final String apiKey;

    public ApiKeyAuthentication(String apiKey, boolean authenticated) {
        super(Collections.emptyList()); // No roles/authorities needed for simple API Key
        this.apiKey = apiKey;
        setAuthenticated(authenticated);
    }

    @Override
    public Object getCredentials() {
        return null; // Don't expose the key as a credential
    }

    @Override
    public Object getPrincipal() {
        return this.apiKey; // The key itself serves as the principal
    }
}
