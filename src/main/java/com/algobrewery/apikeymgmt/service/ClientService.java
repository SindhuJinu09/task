package com.algobrewery.apikeymgmt.service;

import com.algobrewery.apikeymgmt.entity.Client;
import com.algobrewery.apikeymgmt.dto.ClientUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ClientService {
    Client registerClient(Client client, String createdBy);
    String generateApiKeyForClient(String clientId, String createdBy);
    void revokeApiKey(UUID apiKeyId, String revokedBy);
    List<Map<String, Object>> getApiKeysForClient(String clientId);
    Optional<Client> getClientById(String clientId);
    Client updateClient(String clientId, ClientUpdateRequest updateRequest, String updatedBy, HttpServletRequest request);
    String rotateApiKey(String clientId, String performedBy, HttpServletRequest request);
    String generateApiKeyForClient(String clientId, String createdBy, HttpServletRequest request);
} 