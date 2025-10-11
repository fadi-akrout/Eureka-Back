package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampaignSentimentAnalysisResponseDTO {
    
    private boolean success;
    private Integer campaignId;
    
    // Sentiment analysis data
    private SentimentAnalysisData sentimentAnalysisData;
    private SentimentAnalysisSummary sentimentAnalysisSummary;
    
    // Metadata
    private LocalDateTime lastUpdated;
    private Integer totalFeedbacks;
    private Integer analyzedFeedbacks;
    
    // Campaign info
    private String campaignName;
    
    // Error handling
    private String errorMessage;
    
    // Constructor for successful response
    public CampaignSentimentAnalysisResponseDTO(boolean success, Integer campaignId, 
                                               SentimentAnalysisData sentimentAnalysisData,
                                               SentimentAnalysisSummary sentimentAnalysisSummary,
                                               LocalDateTime lastUpdated, Integer totalFeedbacks, 
                                               Integer analyzedFeedbacks, String campaignName) {
        this.success = success;
        this.campaignId = campaignId;
        this.sentimentAnalysisData = sentimentAnalysisData;
        this.sentimentAnalysisSummary = sentimentAnalysisSummary;
        this.lastUpdated = lastUpdated;
        this.totalFeedbacks = totalFeedbacks;
        this.analyzedFeedbacks = analyzedFeedbacks;
        this.campaignName = campaignName;
    }
    
    // Constructor for error response
    public CampaignSentimentAnalysisResponseDTO(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
}