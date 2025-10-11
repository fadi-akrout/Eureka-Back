package org.example.eurekaback.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveSentimentAnalysisRequestDTO {
    
    @NotNull(message = "Campaign ID is required")
    @Positive(message = "Campaign ID must be positive")
    private Integer campaignId;
    
    @Valid
    @NotNull(message = "Sentiment analysis data is required")
    private SentimentAnalysisData sentimentAnalysisData;
    
    @Valid
    @NotNull(message = "Sentiment analysis summary is required")
    private SentimentAnalysisSummary sentimentAnalysisSummary;
    
    @NotNull(message = "Total feedbacks count is required")
    private Integer totalFeedbacks;
    
    @NotNull(message = "Analyzed feedbacks count is required")
    private Integer analyzedFeedbacks;
}