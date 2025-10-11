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
public class SentimentAnalysisSummary {
    private String overallSentiment;
    private SentimentDistribution sentimentDistribution;
    private SentimentPercentages sentimentPercentages;
    private Double averageConfidence;
    private String confidenceLevel;
    private List<String> keyInsights;
    private SentimentTrends sentimentTrends;
    private AnalysisMetadata analysisMetadata;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SentimentDistribution {
        private Integer positive;
        private Integer negative;
        private Integer neutral;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SentimentPercentages {
        private Double positive;
        private Double negative;
        private Double neutral;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SentimentTrends {
        private List<String> mostPositiveAspects;
        private List<String> mostNegativeAspects;
        private List<String> neutralAspects;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnalysisMetadata {
        private String aiModel;
        private LocalDateTime analysisDate;
        private String processingTime;
        private String apiVersion;
    }
}