package org.example.eurekaback.Service.ServiceAI;

import org.example.eurekaback.Dto.SentimentAnalysisRequestDTO;
import org.example.eurekaback.Dto.SentimentAnalysisResponseDTO;

public interface SentimentAnalysisService {
    
    /**
     * Analyze sentiment of text
     */
    SentimentAnalysisResponseDTO analyzeSentiment(SentimentAnalysisRequestDTO request);
    
    /**
     * Check if sentiment analysis service is available
     */
    boolean isServiceAvailable();
    
    /**
     * Analyze multiple texts in batch (optional for future enhancement)
     */
    // List<SentimentAnalysisResponseDTO> analyzeSentimentBatch(List<SentimentAnalysisRequestDTO> requests);
}