package com.algobrewery.apikeymgmt.service;

import com.algobrewery.apikeymgmt.entity.Client;
import com.algobrewery.apikeymgmt.dto.ClientUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClientService {
    CompletableFuture<Client> registerClient(Client client, String createdBy);
    CompletableFuture<String> generateApiKeyForClient(String clientId, String createdBy);
    CompletableFuture<Void> revokeApiKey(UUID apiKeyId, String revokedBy);
    CompletableFuture<List<Map<String, Object>>> getApiKeysForClient(String clientId);
    CompletableFuture<Optional<Client>> getClientById(String clientId);
    CompletableFuture<Client> updateClient(String clientId, ClientUpdateRequest updateRequest, String updatedBy, HttpServletRequest request);
    CompletableFuture<String> rotateApiKey(String clientId, String performedBy, HttpServletRequest request);
    CompletableFuture<String> generateApiKeyForClient(String clientId, String createdBy, HttpServletRequest request);
} 