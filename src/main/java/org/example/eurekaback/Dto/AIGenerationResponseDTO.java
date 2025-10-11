package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AIGenerationResponseDTO {
    
    private boolean success;
    
    private String generatedText;
    
    private String errorMessage;
    
    // Constructor for successful response
    public AIGenerationResponseDTO(String generatedText) {
        this.success = true;
        this.generatedText = generatedText;
    }
    
    // Constructor for error response
    public AIGenerationResponseDTO(String errorMessage, boolean isError) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
}