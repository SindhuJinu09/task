package com.algobrewery.apikeymgmt.security;

import com.algobrewery.apikeymgmt.service.ApiKeyAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Autowired
    private ApiKeyAuthService apiKeyAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("x-api-key");
        if (apiKey != null && apiKeyAuthService.validateApiKey(apiKey)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().startsWith("/clients")) {
            // Allow client registration endpoints without API key
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
} 