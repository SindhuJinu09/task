package com.example.apikeymgmt.repository;

import com.example.apikeymgmt.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByApiKeyHashAndStatus(String apiKeyHash, String status);
    List<ApiKey> findByClientId(String clientId);
}