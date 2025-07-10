package com.algobrewery.apikeymgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class ApiKey {
    @Id
    @GeneratedValue
    private UUID apiKeyId;
    private String clientId;
    private String apiKeyHash;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private String createdBy;
    private String metadata;
}