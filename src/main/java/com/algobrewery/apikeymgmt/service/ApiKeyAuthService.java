package com.algobrewery.apikeymgmt.service;

import com.algobrewery.apikeymgmt.entity.ApiKey;
import com.algobrewery.apikeymgmt.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ApiKeyAuthService {
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public boolean validateApiKey(String apiKey) {
        String hash = ApiKeyUtil.hashApiKey(apiKey);
        Optional<ApiKey> key = apiKeyRepository.findByApiKeyHashAndStatus(hash, "active");
        
        if (key.isPresent()) {
            ApiKey apiKeyEntity = key.get();
            
            // Check if key has expired
            if (apiKeyEntity.getExpiresAt() != null && 
                LocalDateTime.now().isAfter(apiKeyEntity.getExpiresAt())) {
                // Mark expired key as revoked
                apiKeyEntity.setStatus("expired");
                apiKeyRepository.save(apiKeyEntity);
                return false;
            }
            
            // Update last used timestamp
            apiKeyEntity.setLastUsedAt(LocalDateTime.now());
            apiKeyRepository.save(apiKeyEntity);
            
            return true;
        }
        
        return false;
    }
} 