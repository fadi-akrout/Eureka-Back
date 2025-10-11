package org.example.eurekaback.Service.ServiceAI;

import org.example.eurekaback.Dto.*;

public interface SentimentAnalysisPersistenceService {
    
    /**
     * Save sentiment analysis results to the database
     */
    SaveSentimentAnalysisResponseDTO saveSentimentAnalysis(SaveSentimentAnalysisRequestDTO request);
    
    /**
     * Retrieve sentiment analysis results for a campaign
     */
    CampaignSentimentAnalysisResponseDTO getCampaignSentimentAnalysis(Integer campaignId);
    
    /**
     * Check if sentiment analysis exists for a campaign
     */
    boolean hasSentimentAnalysis(Integer campaignId);
    
    /**
     * Delete sentiment analysis for a campaign
     */
    boolean deleteSentimentAnalysis(Integer campaignId);
    
    /**
     * Update existing sentiment analysis
     */
    SaveSentimentAnalysisResponseDTO updateSentimentAnalysis(SaveSentimentAnalysisRequestDTO request);
}