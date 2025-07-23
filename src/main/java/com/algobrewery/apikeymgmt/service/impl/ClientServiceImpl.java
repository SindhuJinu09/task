package com.algobrewery.apikeymgmt.service.impl;

import com.algobrewery.apikeymgmt.entity.Client;
import com.algobrewery.apikeymgmt.entity.ApiKey;
import com.algobrewery.apikeymgmt.entity.AuditLog;
import com.algobrewery.apikeymgmt.repository.ClientRepository;
import com.algobrewery.apikeymgmt.repository.ApiKeyRepository;
import com.algobrewery.apikeymgmt.repository.AuditLogRepository;
import com.algobrewery.apikeymgmt.dto.ClientUpdateRequest;
import com.algobrewery.apikeymgmt.service.ApiKeyUtil;
import com.algobrewery.apikeymgmt.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public CompletableFuture<Client> registerClient(Client client, String createdBy) {
        return CompletableFuture.supplyAsync(() -> {
            client.setClientId(UUID.randomUUID().toString());
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
            client.setStatus("active");
            client.setCreatedBy(createdBy);
            clientRepository.save(client);
            CompletableFuture.runAsync(() ->
                logAudit("Client", client.getClientId(), "CREATE", createdBy, null, null, null)
            , executor);
            return client;
        }, executor);
    }

    @Override
    public CompletableFuture<String> generateApiKeyForClient(String clientId, String createdBy) {
        return generateApiKeyForClient(clientId, createdBy, null);
    }

    @Override
    public CompletableFuture<Void> revokeApiKey(UUID apiKeyId, String revokedBy) {
        return CompletableFuture.runAsync(() -> {
            Optional<ApiKey> keyOpt = apiKeyRepository.findById(apiKeyId);
            if (keyOpt.isPresent()) {
                ApiKey key = keyOpt.get();
                key.setStatus("revoked");
                apiKeyRepository.save(key);
                CompletableFuture.runAsync(() ->
                    logAudit("ApiKey", apiKeyId.toString(), "REVOKE", revokedBy, null, null, null)
                , executor);
            } else {
                throw new RuntimeException("API Key not found: " + apiKeyId);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<Map<String, Object>>> getApiKeysForClient(String clientId) {
        return CompletableFuture.supplyAsync(() -> {
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
        }, executor);
    }

    @Override
    public CompletableFuture<Optional<Client>> getClientById(String clientId) {
        return CompletableFuture.supplyAsync(() -> clientRepository.findById(clientId), executor);
    }

    @Override
    public CompletableFuture<Client> updateClient(String clientId, ClientUpdateRequest updateRequest, String updatedBy, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
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
                CompletableFuture.runAsync(() ->
                    logAudit("Client", clientId, "UPDATE", updatedBy, oldValues + " -> " + newValues, "Client update", request)
                , executor);
                return client;
            }
            throw new RuntimeException("Client not found: " + clientId);
        }, executor);
    }

    @Override
    public CompletableFuture<String> rotateApiKey(String clientId, String performedBy, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            List<ApiKey> activeKeys = apiKeyRepository.findByClientId(clientId);
            for (ApiKey key : activeKeys) {
                if ("active".equals(key.getStatus())) {
                    key.setStatus("revoked");
                    apiKeyRepository.save(key);
                    CompletableFuture.runAsync(() ->
                        logAudit("ApiKey", key.getApiKeyId().toString(), "REVOKE", performedBy, null, "Key rotation", request)
                    , executor);
                }
            }
            String newApiKey = generateApiKeyForClient(clientId, performedBy, request).join();
            CompletableFuture.runAsync(() ->
                logAudit("Client", clientId, "KEY_ROTATION", performedBy, null, "API key rotated", request)
            , executor);
            return newApiKey;
        }, executor);
    }

    @Override
    public CompletableFuture<String> generateApiKeyForClient(String clientId, String createdBy, HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String apiKey = ApiKeyUtil.generateApiKey();
            String hash = ApiKeyUtil.hashApiKey(apiKey);

            ApiKey key = ApiKey.builder()
                    .clientId(clientId)
                    .apiKeyHash(hash)
                    .status("active")
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .build();

            apiKeyRepository.save(key);

            CompletableFuture.runAsync(() ->
                logAudit("ApiKey", key.getApiKeyId().toString(), "CREATE", createdBy, null, null, request)
            , executor);
            return apiKey;
        }, executor);
    }

    private void logAudit(String entityType, String entityId, String action, String performedBy, String changes, String reason, HttpServletRequest request) {
        // R77: Use builder method for AuditLog
        AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .changes(changes)
                .reason(reason);

        // Extract IP address and user agent from request
        if (request != null) {
            logBuilder.ipAddress(getClientIpAddress(request))
                     .userAgent(request.getHeader("User-Agent"));
        }

        AuditLog log = logBuilder.build();
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