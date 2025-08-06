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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{32,}$");
    
    @Autowired
    private ApiKeyAuthService apiKeyAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String clientIP = getClientIPAddress(request);
        
        // Log security event
        logger.info("API request: {} {} from IP: {}", method, requestURI, clientIP);
        
        // Allow health checks and client registration endpoints
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Validate API key
        String apiKey = request.getHeader("x-api-key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Missing API key for request: {} {} from IP: {}", method, requestURI, clientIP);
            sendUnauthorizedResponse(response, "Missing API key");
            return;
        }
        
        // Validate API key format
        if (!isValidApiKeyFormat(apiKey)) {
            logger.warn("Invalid API key format for request: {} {} from IP: {}", method, requestURI, clientIP);
            sendUnauthorizedResponse(response, "Invalid API key format");
            return;
        }
        
        // Validate API key
        try {
            if (apiKeyAuthService.validateApiKey(apiKey)) {
                logger.info("Valid API key for request: {} {} from IP: {}", method, requestURI, clientIP);
                filterChain.doFilter(request, response);
            } else {
                logger.warn("Invalid API key for request: {} {} from IP: {}", method, requestURI, clientIP);
                sendUnauthorizedResponse(response, "Invalid API key");
            }
        } catch (Exception e) {
            logger.error("Error validating API key for request: {} {} from IP: {}", method, requestURI, clientIP, e);
            sendUnauthorizedResponse(response, "Authentication error");
        }
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        // Allow root endpoint and health checks
        if (requestURI.equals("/") || requestURI.equals("/health")) {
            return true;
        }
        
        // Allow client registration (POST /client) but require auth for other client operations
        if (requestURI.equals("/client") || requestURI.equals("/client/")) {
            return true;
        }
        
        // All other endpoints require API key authentication
        return false;
    }
    
    private boolean isValidApiKeyFormat(String apiKey) {
        return apiKey != null && API_KEY_PATTERN.matcher(apiKey).matches();
    }
    
    private String getClientIPAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
    
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    }
} 