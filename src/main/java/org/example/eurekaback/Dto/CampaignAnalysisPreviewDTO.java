package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignAnalysisPreviewDTO {
    
    private boolean success;
    private String errorMessage;
    private Integer campaignId;
    private LocalDateTime analysisDate;
    
    // Analysis results without persistence
    private SentimentAnalysisData sentimentAnalysisData;
    private SentimentAnalysisSummary sentimentAnalysisSummary;
    
    // Analysis statistics
    private int totalFeedbacks;
    private int analyzedFeedbacks;
    private int failedAnalyses;
    private double averageConfidenceScore;
    
    // Quick preview stats
    private Map<String, Integer> sentimentCounts;
    private List<String> topKeywords;
    
    // Constructor for success case
    public CampaignAnalysisPreviewDTO(Integer campaignId, SentimentAnalysisData data, 
                                    SentimentAnalysisSummary summary, int totalFeedbacks, 
                                    int analyzedFeedbacks, int failedAnalyses) {
        this.success = true;
        this.campaignId = campaignId;
        this.sentimentAnalysisData = data;
        this.sentimentAnalysisSummary = summary;
        this.totalFeedbacks = totalFeedbacks;
        this.analyzedFeedbacks = analyzedFeedbacks;
        this.failedAnalyses = failedAnalyses;
        this.analysisDate = LocalDateTime.now();
    }
    
    // Constructor for error case
    public CampaignAnalysisPreviewDTO(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
}