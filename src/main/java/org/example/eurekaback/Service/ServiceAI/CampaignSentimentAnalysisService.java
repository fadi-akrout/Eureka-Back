package org.example.eurekaback.Service.ServiceAI;

import org.example.eurekaback.Dto.*;

import java.util.List;

public interface CampaignSentimentAnalysisService {
    
    /**
     * Analyze sentiment for all feedbacks of a campaign and save results
     */
    SaveSentimentAnalysisResponseDTO analyzeCampaignFeedbacks(Integer campaignId);
    
    /**
     * Analyze sentiment for specific feedbacks and save results
     */
    SaveSentimentAnalysisResponseDTO analyzeFeedbacksBatch(Integer campaignId, List<Long> feedbackIds);
    
    /**
     * Re-analyze all campaign feedbacks (refresh analysis)
     */
    SaveSentimentAnalysisResponseDTO reAnalyzeCampaignFeedbacks(Integer campaignId);
    
    /**
     * Get comprehensive sentiment analysis report for a campaign
     */
    CampaignSentimentAnalysisResponseDTO getCampaignSentimentReport(Integer campaignId);
    
    /**
     * Check if campaign has feedbacks eligible for sentiment analysis
     */
    boolean hasFeedbacksForAnalysis(Integer campaignId);
    
    /**
     * Get count of analyzable feedbacks for a campaign
     */
    int getAnalyzableFeedbacksCount(Integer campaignId);
    
    /**
     * Perform sentiment analysis on campaign feedbacks WITHOUT saving to database
     * Returns analysis results for preview/validation
     */
    CampaignAnalysisPreviewDTO analyzeCampaignFeedbacksPreview(Integer campaignId);
    
    /**
     * Save pre-analyzed sentiment results to database
     * Used after preview/validation step
     */
    SaveSentimentAnalysisResponseDTO saveCampaignAnalysisResults(Integer campaignId, CampaignAnalysisPreviewDTO analysisResults);
}