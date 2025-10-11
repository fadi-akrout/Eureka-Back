package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignAnalyticsDTO {
    
    private Integer campaignId;
    private String campaignName;
    private SentimentDistribution sentimentDistribution;
    private ConfidenceScores confidenceScores;
    private CampaignInsights insights;
    private AdditionalMetrics additionalMetrics;
    private List<KeywordAnalysis> topKeywords;
    private List<DailyTrend> dailyTrends;
    private LocalDateTime generatedAt;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SentimentDistribution {
        private Integer positive;
        private Integer negative;
        private Integer neutral;
        private Integer totalResponses;
        private Integer analyzedResponses;
        private BigDecimal positivePercentage;
        private BigDecimal negativePercentage;
        private BigDecimal neutralPercentage;
        private String overallSentiment;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ConfidenceScores {
        private BigDecimal averageConfidence;
        private Integer highConfidenceCount;
        private Integer mediumConfidenceCount;
        private Integer lowConfidenceCount;
        private BigDecimal highConfidencePercentage;
        private BigDecimal mediumConfidencePercentage;
        private BigDecimal lowConfidencePercentage;
        private String confidenceLevel;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CampaignInsights {
        private List<String> keyFindings;
        private List<String> recommendations;
        private List<String> trends;
        private List<String> riskFactors;
        private String overallSentiment;
        private String confidenceLevel;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AdditionalMetrics {
        private BigDecimal responseRate;
        private BigDecimal completionRate;
        private BigDecimal averageRating;
        private Integer totalInvited;
        private Integer totalResponded;
        private Integer totalCompleted;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class KeywordAnalysis {
        private String keyword;
        private Integer frequency;
        private String sentimentAssociation;
        private BigDecimal confidenceScore;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DailyTrend {
        private String date;
        private Integer responseCount;
        private BigDecimal averageRating;
        private String dominantSentiment;
        private Integer positiveCount;
        private Integer negativeCount;
        private Integer neutralCount;
        private BigDecimal averageConfidence;
    }
}