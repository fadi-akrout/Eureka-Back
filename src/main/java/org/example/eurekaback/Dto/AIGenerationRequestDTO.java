package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AIGenerationRequestDTO {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String category;
    
    private String targetAudience;
    
    private String keyFeatures;
    
    private String additionalContext;
}