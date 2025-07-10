package com.algobrewery.apikeymgmt.controller;

import com.algobrewery.apikeymgmt.entity.Client;
import com.algobrewery.apikeymgmt.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<?> registerClient(@RequestBody Client client) {
        // For demo, use 'system' as createdBy
        Client saved = clientService.registerClient(client, "system");
        String apiKey = clientService.generateApiKeyForClient(saved.getClientId(), "system");
        return ResponseEntity.ok(Map.of(
                "client_id", saved.getClientId(),
                "api_key", apiKey,
                "status", saved.getStatus(),
                "created_at", saved.getCreatedAt()
        ));
    }

    @PostMapping("/{clientId}/api-keys")
    public ResponseEntity<?> generateApiKey(@PathVariable String clientId) {
        String apiKey = clientService.generateApiKeyForClient(clientId, "system");
        return ResponseEntity.ok(Map.of(
                "client_id", clientId,
                "api_key", apiKey
        ));
    }

    @PostMapping("/api-keys/{apiKeyId}/revoke")
    public ResponseEntity<?> revokeApiKey(@PathVariable UUID apiKeyId) {
        clientService.revokeApiKey(apiKeyId, "system");
        return ResponseEntity.ok(Map.of("revoked", true));
    }

    @GetMapping("/{clientId}/api-keys")
    public ResponseEntity<?> getApiKeys(@PathVariable String clientId) {
        var apiKeys = clientService.getApiKeysForClient(clientId);
        return ResponseEntity.ok(apiKeys);
    }
}