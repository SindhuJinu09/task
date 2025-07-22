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

        // Create client entity from request
        Client client = new Client();
        client.setClientName(request.getClientName());
        client.setClientType(request.getClientType());
        client.setOrganizationUuid(companyId);
        if (request.getMetadata() != null) {
            client.setMetadata(request.getMetadata().toString());
        }

        Client saved = clientService.registerClient(client, userId);
        String apiKey = clientService.generateApiKeyForClient(saved.getClientId(), userId, httpRequest);

        return ResponseEntity.ok(Map.of(
                "client_id", saved.getClientId(),
                "api_key", apiKey,
                "client_name", saved.getClientName(),
                "status", saved.getStatus(),
                "created_at", saved.getCreatedAt()
        ));
    }

    @PostMapping("/{clientId}/api-key")
    public ResponseEntity<?> generateApiKey(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId,
            HttpServletRequest httpRequest) {
        String apiKey = clientService.generateApiKeyForClient(clientId, userId, httpRequest);
        return ResponseEntity.ok(Map.of(
                "client_id", clientId,
                "api_key", apiKey
        ));
    }

    @PostMapping("/api-key/{apiKeyId}/revoke")
    public ResponseEntity<?> revokeApiKey(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable UUID apiKeyId) {
        clientService.revokeApiKey(apiKeyId, userId);
        return ResponseEntity.ok(Map.of("revoked", true));
    }

    @GetMapping("/{clientId}/api-key")
    public ResponseEntity<?> getApiKeys(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId) {
        var apiKeys = clientService.getApiKeysForClient(clientId);
        return ResponseEntity.ok(apiKeys);
    }

    // NEW ENDPOINTS - Missing from original implementation

    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClient(
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-company-id") String companyId,
            @RequestHeader("x-request-id") String requestId,
            @PathVariable String clientId) {
        var client = clientService.getClientById(clientId);
        if (client.isPresent()) {
            Client c = client.get();
            ClientResponse response = new ClientResponse();
            response.setClientId(c.getClientId());
            response.setClientName(c.getClientName());
            response.setClientType(c.getClientType());
            response.setStatus(c.getStatus());
            response.setOrganizationUuid(c.getOrganizationUuid());
            response.setCreatedAt(c.getCreatedAt());
            response.setUpdatedAt(c.getUpdatedAt());
            response.setCreatedBy(c.getCreatedBy());

            // Handle metadata conversion from String to Map
            if (c.getMetadata() != null && !c.getMetadata().isEmpty()) {
                try {
                    // Parse JSON string to Map (you might want to use Jackson ObjectMapper for this)
                    response.setMetadata(parseMetadataString(c.getMetadata()));
                } catch (Exception e) {
                    // If parsing fails, create a simple map with the raw string
                    response.setMetadata(Map.of("raw", c.getMetadata()));
                }
            }

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
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
            Client updated = clientService.updateClient(clientId, updateRequest, userId, httpRequest);
            ClientResponse response = new ClientResponse();
            response.setClientId(updated.getClientId());
            response.setClientName(updated.getClientName());
            response.setClientType(updated.getClientType());
            response.setStatus(updated.getStatus());
            response.setOrganizationUuid(updated.getOrganizationUuid());
            response.setCreatedAt(updated.getCreatedAt());
            response.setUpdatedAt(updated.getUpdatedAt());
            response.setCreatedBy(updated.getCreatedBy());

            // Handle metadata conversion from String to Map
            if (updated.getMetadata() != null && !updated.getMetadata().isEmpty()) {
                try {
                    response.setMetadata(parseMetadataString(updated.getMetadata()));
                } catch (Exception e) {
                    response.setMetadata(Map.of("raw", updated.getMetadata()));
                }
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
            String newApiKey = clientService.rotateApiKey(clientId, userId, httpRequest);
            return ResponseEntity.ok(Map.of(
                    "client_id", clientId,
                    "api_key", newApiKey,
                    "message", "API key rotated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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