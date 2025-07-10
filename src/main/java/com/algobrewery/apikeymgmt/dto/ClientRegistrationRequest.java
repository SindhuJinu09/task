package com.algobrewery.apikeymgmt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ClientRegistrationRequest {
    private String clientName;
    private String clientType;
    private List<String> redirectUris;
    private String publicKey;
    private Map<String, Object> metadata;
}
