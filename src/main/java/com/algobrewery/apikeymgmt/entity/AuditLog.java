package com.algobrewery.apikeymgmt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue
    private UUID auditId;
    private String entityType;
    private String entityId;
    private String action;
    private String performedBy;
    private LocalDateTime performedAt;
    private String changes;
    private String reason;
    private String ipAddress;
    private String userAgent;
}