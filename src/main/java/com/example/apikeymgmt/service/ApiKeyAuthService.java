package com.example.apikeymgmt.service;

import com.example.apikeymgmt.entity.ApiKey;
import com.example.apikeymgmt.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiKeyAuthService {
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public boolean validateApiKey(String apiKey) {
        String hash = ApiKeyUtil.hashApiKey(apiKey);
        Optional<ApiKey> key = apiKeyRepository.findByApiKeyHashAndStatus(hash, "active");
        return key.isPresent();
    }
} 