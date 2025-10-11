package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SentimentAnalysisData {
    private List<FeedbackSentiment> feedbackSentiments;
    private BatchAnalysisInfo batchAnalysis;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedbackSentiment {
        private Long feedbackId;
        private String sentiment;
        private Double confidenceScore;
        private SentimentScores scores;
        private String insights;
        private LocalDateTime analyzedAt;
        private Integer textLength;
        private List<String> keywords;
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
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchAnalysisInfo {
        private Integer totalBatches;
        private Integer successfulAnalyses;
        private Integer failedAnalyses;
        private String averageProcessingTime;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}