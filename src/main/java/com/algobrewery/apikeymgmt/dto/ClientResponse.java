package com.algobrewery.apikeymgmt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ClientResponse {
    private String clientId;
    private String clientName;
    private String clientType;
    private String status;
    private String organizationUuid;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
