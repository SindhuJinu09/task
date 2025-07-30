package com.algobrewery.apikeymgmt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ClientRegistrationRequest {
    @NotBlank(message = "Client name is required")
    @Size(min = 1, max = 100, message = "Client name must be between 1 and 100 characters")
    private String clientName;
    
    @NotBlank(message = "Client type is required")
    @Pattern(regexp = "^(INTERNAL|EXTERNAL|SYSTEM)$", message = "Client type must be INTERNAL, EXTERNAL, or SYSTEM")
    private String clientType;
    
    private List<String> redirectUris;
    private String publicKey;
    private Map<String, Object> metadata;
}
