package com.algobrewery.apikeymgmt.controller;

import com.algobrewery.apikeymgmt.entity.Client;
import com.algobrewery.apikeymgmt.service.ClientService;
import com.algobrewery.apikeymgmt.dto.ClientRegistrationRequest;
import com.algobrewery.apikeymgmt.dto.ClientUpdateRequest;
import com.algobrewery.apikeymgmt.dto.ClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/client")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<?> registerClient(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @RequestBody ClientRegistrationRequest request,
            HttpServletRequest httpRequest) {
        try {
            Client client = new Client();
            client.setClientName(request.getClientName());
            client.setClientType(request.getClientType());
            client.setOrganizationUuid(companyId);
            if (request.getMetadata() != null) {
                client.setMetadata(request.getMetadata().toString());
            }
            Client saved = clientService.registerClient(client, userId).get();
            String apiKey = clientService.generateApiKeyForClient(saved.getClientId(), userId, httpRequest).get();
            return ResponseEntity.ok(Map.of(
                    "client_id", saved.getClientId(),
                    "api_key", apiKey,
                    "client_name", saved.getClientName(),
                    "status", saved.getStatus(),
                    "created_at", saved.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{clientId}/api-key")
    public ResponseEntity<?> generateApiKey(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId,
            HttpServletRequest httpRequest) {
        try {
            String apiKey = clientService.generateApiKeyForClient(clientId, userId, httpRequest).get();
            return ResponseEntity.ok(Map.of(
                    "client_id", clientId,
                    "api_key", apiKey
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api-key/{apiKeyId}/revoke")
    public ResponseEntity<?> revokeApiKey(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable UUID apiKeyId) {
        try {
            clientService.revokeApiKey(apiKeyId, userId).get();
            return ResponseEntity.ok(Map.of("revoked", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{clientId}/api-key")
    public ResponseEntity<?> getApiKeys(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId) {
        try {
            Object apiKeys = clientService.getApiKeysForClient(clientId).get();
            return ResponseEntity.ok(apiKeys);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // NEW ENDPOINTS - Missing from original implementation

    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClient(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId) {
        try {
            var clientOpt = clientService.getClientById(clientId).get();
            if (clientOpt.isPresent()) {
                Client c = clientOpt.get();
                ClientResponse response = new ClientResponse();
                response.setClientId(c.getClientId());
                response.setClientName(c.getClientName());
                response.setClientType(c.getClientType());
                response.setStatus(c.getStatus());
                response.setOrganizationUuid(c.getOrganizationUuid());
                response.setCreatedAt(c.getCreatedAt());
                response.setUpdatedAt(c.getUpdatedAt());
                response.setCreatedBy(c.getCreatedBy());
                if (c.getMetadata() != null && !c.getMetadata().isEmpty()) {
                    try {
                        response.setMetadata(parseMetadataString(c.getMetadata()));
                    } catch (Exception e) {
                        response.setMetadata(Map.of("raw", c.getMetadata()));
                    }
                }
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<?> updateClient(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId,
            @RequestBody ClientUpdateRequest updateRequest,
            HttpServletRequest httpRequest) {
        try {
            Client updated = clientService.updateClient(clientId, updateRequest, userId, httpRequest).get();
            ClientResponse response = new ClientResponse();
            response.setClientId(updated.getClientId());
            response.setClientName(updated.getClientName());
            response.setClientType(updated.getClientType());
            response.setStatus(updated.getStatus());
            response.setOrganizationUuid(updated.getOrganizationUuid());
            response.setCreatedAt(updated.getCreatedAt());
            response.setUpdatedAt(updated.getUpdatedAt());
            response.setCreatedBy(updated.getCreatedBy());
            if (updated.getMetadata() != null && !updated.getMetadata().isEmpty()) {
                try {
                    response.setMetadata(parseMetadataString(updated.getMetadata()));
                } catch (Exception e) {
                    response.setMetadata(Map.of("raw", updated.getMetadata()));
                }
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{clientId}/rotate-key")
    public ResponseEntity<?> rotateApiKey(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId,
            HttpServletRequest httpRequest) {
        try {
            String newApiKey = clientService.rotateApiKey(clientId, userId, httpRequest).get();
            return ResponseEntity.ok(Map.of(
                    "client_id", clientId,
                    "api_key", newApiKey,
                    "message", "API key rotated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Removed: @GetMapping("/clients")
    // public ResponseEntity<?> getAllClients(
    //         @RequestHeader("x-user-id") String userId,
    //         @RequestHeader("x-company-id") String companyId,
    //         @RequestHeader("x-request-id") String requestId) {
    //     var clients = clientService.getAllClients();
    //     return ResponseEntity.ok(clients);
    // }

    // Helper method to parse metadata string to Map
    private Map<String, Object> parseMetadataString(String metadataString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(metadataString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // If it's not valid JSON, return it as a simple key-value pair
            return Map.of("value", metadataString);
        }
    }
}