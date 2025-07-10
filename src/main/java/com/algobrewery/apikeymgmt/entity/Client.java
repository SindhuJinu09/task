package com.algobrewery.apikeymgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Client {
    @Id
    private String clientId;
    private String clientName;
    private String clientType;
    private String status;
    private String organizationUuid;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}