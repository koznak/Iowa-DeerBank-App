package com.deerbank.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService         jwtService;
//    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {


        //read token from the authorization header
        String authHeader = request.getHeader("Authorization");
//        System.out.println("JWT Filter url : "+ request.getRequestURI());
//        if(!(request.getRequestURI().equals("/api/auth/login") || request.getRequestURI().equals("/api/auth/register"))){
        if (authHeader != null && authHeader.startsWith("Bearer ") ) {
            String token = authHeader.substring(7);
            //Get username from the token
            String username= jwtService.parseSignedClaims(token).getSubject();

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                SecurityContextHolder.getContext()
                                     .setAuthentication(
                                             new UsernamePasswordAuthenticationToken(
                                                     username,
                                                     null,
                                                     null));
            }

        }

        filterChain.doFilter(request, response);

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        return request.getServletPath().contains("/api/v1/auth");
    }
}
