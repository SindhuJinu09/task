package com.algobrewery.apikeymgmt.service;

import com.algobrewery.apikeymgmt.entity.Client;
import com.algobrewery.apikeymgmt.entity.ApiKey;
import com.algobrewery.apikeymgmt.entity.AuditLog;
import com.algobrewery.apikeymgmt.repository.ClientRepository;
import com.algobrewery.apikeymgmt.repository.ApiKeyRepository;
import com.algobrewery.apikeymgmt.repository.AuditLogRepository;
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
        client.setStatus("active");
        client.setCreatedBy(createdBy);
        clientRepository.save(client);
        logAudit("Client", client.getClientId(), "CREATE", createdBy, null, null);
        return client;
    }

    public String generateApiKeyForClient(String clientId, String createdBy) {
        String apiKey = ApiKeyUtil.generateApiKey();
        String hash = ApiKeyUtil.hashApiKey(apiKey);
        ApiKey key = new ApiKey();
        key.setClientId(clientId);
        key.setApiKeyHash(hash);
        key.setStatus("active");
        key.setCreatedAt(LocalDateTime.now());
        key.setCreatedBy(createdBy);
        apiKeyRepository.save(key);
        logAudit("ApiKey", key.getApiKeyId().toString(), "CREATE", createdBy, null, null);
        return apiKey;
    }

    public void revokeApiKey(UUID apiKeyId, String revokedBy) {
        Optional<ApiKey> keyOpt = apiKeyRepository.findById(apiKeyId);
        if (keyOpt.isPresent()) {
            ApiKey key = keyOpt.get();
            key.setStatus("revoked");
            apiKeyRepository.save(key);
            logAudit("ApiKey", apiKeyId.toString(), "REVOKE", revokedBy, null, null);
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

    private void logAudit(String entityType, String entityId, String action, String performedBy, String changes, String reason) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(LocalDateTime.now());
        log.setChanges(changes);
        log.setReason(reason);
        auditLogRepository.save(log);
    }
} 