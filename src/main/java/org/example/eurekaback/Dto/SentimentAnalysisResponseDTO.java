package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SentimentAnalysisResponseDTO {
    
    private boolean success;
    
    // Sentiment: POSITIVE, NEGATIVE, NEUTRAL
    private String sentiment;
    
    // Confidence score (0.0 to 1.0)
    private Double confidenceScore;
    
    // Detailed sentiment scores
    private SentimentScores scores;
    
    // Error message if any
    private String errorMessage;
    
    // Additional insights (optional)
    private String insights;
    
    // Constructor for successful response
    public SentimentAnalysisResponseDTO(String sentiment, Double confidenceScore, SentimentScores scores) {
        this.success = true;
        this.sentiment = sentiment;
        this.confidenceScore = confidenceScore;
        this.scores = scores;
    }
    
    // Constructor for successful response with insights
    public SentimentAnalysisResponseDTO(String sentiment, Double confidenceScore, SentimentScores scores, String insights) {
        this.success = true;
        this.sentiment = sentiment;
        this.confidenceScore = confidenceScore;
        this.scores = scores;
        this.insights = insights;
    }
    
    // Constructor for error response
    public SentimentAnalysisResponseDTO(String errorMessage, boolean isError) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SentimentScores {
        private Double positive;
        private Double negative;
        private Double neutral;
    }
}