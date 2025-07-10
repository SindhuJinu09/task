package com.algobrewery.apikeymgmt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ClientUpdateRequest {
    private String clientName;
    private String status;
    private Map<String, Object> metadata;
}
