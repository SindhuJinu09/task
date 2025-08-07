package com.algobrewery.apikeymgmt.controller;

import com.algobrewery.apikeymgmt.service.ApiKeyAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for API key validation endpoints
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ValidationController {

    @Autowired
    private ApiKeyAuthService apiKeyAuthService;

    /**
     * Validate an API key
     * This endpoint is used by other services to validate API keys
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateApiKey(@RequestHeader("x-api-key") String apiKey) {
        boolean isValid = apiKeyAuthService.validateApiKey(apiKey);
        
        if (isValid) {
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "API key is valid"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                "valid", false,
                "message", "API key is invalid or expired"
            ));
        }
    }

    /**
     * Simple health check endpoint for validation service
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "api-key-validation"
        ));
    }
}