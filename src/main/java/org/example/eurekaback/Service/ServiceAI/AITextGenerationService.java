package org.example.eurekaback.Service.ServiceAI;

import org.example.eurekaback.Dto.AIGenerationRequestDTO;
import org.example.eurekaback.Dto.AIGenerationResponseDTO;

public interface AITextGenerationService {
    
    /**
     * Generate campaign description
     */
    AIGenerationResponseDTO generateCampaignDescription(AIGenerationRequestDTO request);
    
    /**
     * Generate product description
     */
    AIGenerationResponseDTO generateProductDescription(AIGenerationRequestDTO request);
    
    /**
     * Check if AI service is available
     */
    boolean isServiceAvailable();
}