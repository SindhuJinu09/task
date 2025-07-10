package com.algobrewery.apikeymgmt.service;

import com.algobrewery.apikeymgmt.entity.Client;
import com.algobrewery.apikeymgmt.entity.ApiKey;
import com.algobrewery.apikeymgmt.entity.AuditLog;
import com.algobrewery.apikeymgmt.repository.ClientRepository;
import com.algobrewery.apikeymgmt.repository.ApiKeyRepository;
import com.algobrewery.apikeymgmt.repository.AuditLogRepository;
import com.algobrewery.apikeymgmt.dto.ClientUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    public Client registerClient(Client client, String createdBy) {
        client.setClientId(UUID.randomUUID().toString());
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        client.setStatus("active");
        client.setCreatedBy(createdBy);
        clientRepository.save(client);
        logAudit("Client", client.getClientId(), "CREATE", createdBy, null, null, null);
        return client;
    }

    public String generateApiKeyForClient(String clientId, String createdBy) {
        return generateApiKeyForClient(clientId, createdBy, null);
    }

    public void revokeApiKey(UUID apiKeyId, String revokedBy) {
        Optional<ApiKey> keyOpt = apiKeyRepository.findById(apiKeyId);
        if (keyOpt.isPresent()) {
            ApiKey key = keyOpt.get();
            key.setStatus("revoked");
            apiKeyRepository.save(key);
            logAudit("ApiKey", apiKeyId.toString(), "REVOKE", revokedBy, null, null, null);
        }
    }

    public List<Map<String, Object>> getApiKeysForClient(String clientId) {
        List<ApiKey> apiKeys = apiKeyRepository.findByClientId(clientId);
        return apiKeys.stream()
                .<Map<String, Object>>map(key -> Map.of(
                        "api_key_id", key.getApiKeyId().toString(),
                        "client_id", key.getClientId(),
                        "status", key.getStatus(),
                        "created_at", key.getCreatedAt().toString(),
                        "created_by", key.getCreatedBy() != null ? key.getCreatedBy() : ""
                ))
                .collect(Collectors.toList());
    }

    // Get client by ID
    public Optional<Client> getClientById(String clientId) {
        return clientRepository.findById(clientId);
    }

    // Update client
    public Client updateClient(String clientId, ClientUpdateRequest updateRequest, String updatedBy, HttpServletRequest request) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            String oldValues = String.format("name:%s,status:%s", client.getClientName(), client.getStatus());

            if (updateRequest.getClientName() != null) {
                client.setClientName(updateRequest.getClientName());
            }
            if (updateRequest.getStatus() != null) {
                client.setStatus(updateRequest.getStatus());
            }
            if (updateRequest.getMetadata() != null) {
                client.setMetadata(updateRequest.getMetadata().toString());
            }
            client.setUpdatedAt(LocalDateTime.now());

            clientRepository.save(client);
            String newValues = String.format("name:%s,status:%s", client.getClientName(), client.getStatus());
            logAudit("Client", clientId, "UPDATE", updatedBy, oldValues + " -> " + newValues, "Client update", request);
            return client;
        }
        throw new RuntimeException("Client not found: " + clientId);
    }

    // Rotate API key
    public String rotateApiKey(String clientId, String performedBy, HttpServletRequest request) {
        // Revoke all existing active keys for this client
        List<ApiKey> activeKeys = apiKeyRepository.findByClientId(clientId);
        for (ApiKey key : activeKeys) {
            if ("active".equals(key.getStatus())) {
                key.setStatus("revoked");
                apiKeyRepository.save(key);
                logAudit("ApiKey", key.getApiKeyId().toString(), "REVOKE", performedBy, null, "Key rotation", request);
            }
        }

        // Generate new key
        String newApiKey = generateApiKeyForClient(clientId, performedBy, request);
        logAudit("Client", clientId, "KEY_ROTATION", performedBy, null, "API key rotated", request);
        return newApiKey;
    }

    // Enhanced generateApiKeyForClient with request context
    public String generateApiKeyForClient(String clientId, String createdBy, HttpServletRequest request) {
        String apiKey = ApiKeyUtil.generateApiKey();
        String hash = ApiKeyUtil.hashApiKey(apiKey);
        ApiKey key = new ApiKey();
        key.setClientId(clientId);
        key.setApiKeyHash(hash);
        key.setStatus("active");
        key.setCreatedAt(LocalDateTime.now());
        key.setCreatedBy(createdBy);
        apiKeyRepository.save(key);
        logAudit("ApiKey", key.getApiKeyId().toString(), "CREATE", createdBy, null, null, request);
        return apiKey;
    }

    // Get all clients with pagination
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    private void logAudit(String entityType, String entityId, String action, String performedBy, String changes, String reason, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(LocalDateTime.now());
        log.setChanges(changes);
        log.setReason(reason);

        // Extract IP address and user agent from request
        if (request != null) {
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        auditLogRepository.save(log);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
} 