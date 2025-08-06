package com.algobrewery.apikeymgmt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "apikey")
public class ApiKeyConfig {
    
    private Duration expiryDuration = Duration.ofDays(365); // Default 1 year
    private boolean allowMultipleActiveKeys = false; // Default to single active key
    
    public Duration getExpiryDuration() {
        return expiryDuration;
    }
    
    public void setExpiryDuration(Duration expiryDuration) {
        this.expiryDuration = expiryDuration;
    }
    
    public boolean isAllowMultipleActiveKeys() {
        return allowMultipleActiveKeys;
    }
    
    public void setAllowMultipleActiveKeys(boolean allowMultipleActiveKeys) {
        this.allowMultipleActiveKeys = allowMultipleActiveKeys;
    }
} 