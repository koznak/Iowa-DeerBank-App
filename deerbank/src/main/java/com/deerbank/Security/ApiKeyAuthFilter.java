package com.deerbank.Security;
// ApiKeyAuthFilter.java (Revised)
import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter responsible for extracting the X-API-Key from the header,
 * creating an Authentication object, and submitting it to the
 * AuthenticationManager for validation.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String headerName;
    private final AuthenticationManager authenticationManager;

    /**
     * The AuthenticationManager is injected here via the SecurityConfig.
     */
    public ApiKeyAuthFilter(String headerName, AuthenticationManager authenticationManager) {
        this.headerName = headerName;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException { // <-- Correct Signature

        // 1. Extract the API Key from the custom header
        String requestApiKey = request.getHeader(headerName);

        // If no API Key is provided, allow the request to proceed.
        // Spring Security will later block it as 'unauthenticated' if the path requires it.
        if (requestApiKey == null || requestApiKey.isEmpty()) {
            //filterChain.doFilter(request, response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }

        try {
            // 2. Create an unauthenticated token with the extracted key
            ApiKeyAuthentication authentication = new ApiKeyAuthentication(requestApiKey, false);

            // 3. Submit the token to the AuthenticationManager (which uses ApiKeyAuthenticationProvider)
            Authentication authenticated = this.authenticationManager.authenticate(authentication);

            // 4. Set the fully authenticated object in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticated);

        } catch (AuthenticationException failed) {
            // 5. Handle invalid key: clear context and send a 401 Unauthorized
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }

        // Proceed to the next filter in the chain (e.g., authorization)
        filterChain.doFilter(request, response);
    }
}