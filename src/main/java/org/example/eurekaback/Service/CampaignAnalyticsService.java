package org.example.eurekaback.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.*;
import org.example.eurekaback.Entity.*;
import org.example.eurekaback.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CampaignAnalyticsService {

    private final CampagneRepository campagneRepository;
    private final FeedbackRepository feedbackRepository;
    private final ResultatAnalyseRepository resultatAnalyseRepository;
    private final PanelisteCampagneRepository panelisteCampagneRepository;

    /**
     * Generate comprehensive analytics for a campaign
     */
    @Transactional(readOnly = true)
    public CampaignAnalyticsResponseDTO generateCampaignAnalytics(CampaignAnalyticsRequestDTO request) {
        try {
            log.info("Generating analytics for campaign ID: {}", request.getCampaignId());
            
            // Verify campaign exists
            Optional<Campagne> campagneOpt = campagneRepository.findById(request.getCampaignId());
            if (campagneOpt.isEmpty()) {
                return new CampaignAnalyticsResponseDTO("Campaign not found with ID: " + request.getCampaignId());
            }
            
            Campagne campagne = campagneOpt.get();
            
            // Get sentiment analysis data
            Optional<ResultatAnalyse> resultatAnalyseOpt = resultatAnalyseRepository.findByCampaignId(request.getCampaignId());
            if (resultatAnalyseOpt.isEmpty() || resultatAnalyseOpt.get().getSentimentAnalysisData() == null) {
                return new CampaignAnalyticsResponseDTO("No sentiment analysis data found for campaign ID: " + request.getCampaignId());
            }
            
            ResultatAnalyse resultatAnalyse = resultatAnalyseOpt.get();
            SentimentAnalysisData sentimentData = resultatAnalyse.getSentimentAnalysisData();
            SentimentAnalysisSummary sentimentSummary = resultatAnalyse.getSentimentAnalysisSummary();
            
            // Build comprehensive analytics
            CampaignAnalyticsDTO analyticsDTO = buildAnalyticsDTO(campagne, sentimentData, sentimentSummary, request);
            
            log.info("Successfully generated analytics for campaign ID: {}", request.getCampaignId());
            return new CampaignAnalyticsResponseDTO(analyticsDTO);
            
        } catch (Exception e) {
            log.error("Error generating analytics for campaign ID: {}", request.getCampaignId(), e);
            return new CampaignAnalyticsResponseDTO("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Build the complete analytics DTO
     */
    private CampaignAnalyticsDTO buildAnalyticsDTO(Campagne campagne, 
                                                  SentimentAnalysisData sentimentData, 
                                                  SentimentAnalysisSummary sentimentSummary,
                                                  CampaignAnalyticsRequestDTO request) {
        
        CampaignAnalyticsDTO.CampaignAnalyticsDTOBuilder builder = CampaignAnalyticsDTO.builder()
            .campaignId(campagne.getId())
            .campaignName(campagne.getNom())
            .sentimentDistribution(buildSentimentDistribution(sentimentSummary, sentimentData))
            .confidenceScores(buildConfidenceScores(sentimentData))
            .insights(buildInsights(sentimentData, sentimentSummary))
            .additionalMetrics(buildAdditionalMetrics(campagne.getId()))
            .generatedAt(LocalDateTime.now());
        
        // Add keywords if requested
        if (request.isIncludeKeywords()) {
            builder.topKeywords(buildKeywords(sentimentData, request.getMaxKeywords()));
        }
        
        // Add daily trends if requested
        if (request.isIncludeDailyTrends()) {
            builder.dailyTrends(buildDailyTrends(sentimentData, request.getStartDate(), request.getEndDate()));
        }
        
        return builder.build();
    }

    /**
     * Build sentiment distribution from existing data
     */
    private CampaignAnalyticsDTO.SentimentDistribution buildSentimentDistribution(SentimentAnalysisSummary sentimentSummary, 
                                                                                  SentimentAnalysisData sentimentData) {
        
        if (sentimentSummary == null || sentimentSummary.getSentimentDistribution() == null) {
            return CampaignAnalyticsDTO.SentimentDistribution.builder()
                .positive(0)
                .negative(0)
                .neutral(0)
                .totalResponses(0)
                .analyzedResponses(0)
                .positivePercentage(BigDecimal.ZERO)
                .negativePercentage(BigDecimal.ZERO)
                .neutralPercentage(BigDecimal.ZERO)
                .overallSentiment("UNKNOWN")
                .build();
        }
        
        SentimentAnalysisSummary.SentimentDistribution dist = sentimentSummary.getSentimentDistribution();
        int total = dist.getPositive() + dist.getNegative() + dist.getNeutral();
        
        return CampaignAnalyticsDTO.SentimentDistribution.builder()
            .positive(dist.getPositive())
            .negative(dist.getNegative())
            .neutral(dist.getNeutral())
            .totalResponses(total)
            .analyzedResponses(total)
            .positivePercentage(calculatePercentage(dist.getPositive(), total))
            .negativePercentage(calculatePercentage(dist.getNegative(), total))
            .neutralPercentage(calculatePercentage(dist.getNeutral(), total))
            .overallSentiment(determineOverallSentiment(dist))
            .build();
    }

    /**
     * Build confidence scores from sentiment data
     */
    private CampaignAnalyticsDTO.ConfidenceScores buildConfidenceScores(SentimentAnalysisData sentimentData) {
        
        if (sentimentData == null || sentimentData.getFeedbackSentiments() == null) {
            return CampaignAnalyticsDTO.ConfidenceScores.builder()
                .averageConfidence(BigDecimal.ZERO)
                .highConfidenceCount(0)
                .mediumConfidenceCount(0)
                .lowConfidenceCount(0)
                .highConfidencePercentage(BigDecimal.ZERO)
                .mediumConfidencePercentage(BigDecimal.ZERO)
                .lowConfidencePercentage(BigDecimal.ZERO)
                .confidenceLevel("LOW")
                .build();
        }
        
        List<Double> confidenceScores = sentimentData.getFeedbackSentiments().stream()
            .filter(fs -> fs.getConfidenceScore() != null)
            .map(SentimentAnalysisData.FeedbackSentiment::getConfidenceScore)
            .collect(Collectors.toList());
        
        if (confidenceScores.isEmpty()) {
            return CampaignAnalyticsDTO.ConfidenceScores.builder()
                .averageConfidence(BigDecimal.ZERO)
                .highConfidenceCount(0)
                .mediumConfidenceCount(0)
                .lowConfidenceCount(0)
                .highConfidencePercentage(BigDecimal.ZERO)
                .mediumConfidencePercentage(BigDecimal.ZERO)
                .lowConfidencePercentage(BigDecimal.ZERO)
                .confidenceLevel("LOW")
                .build();
        }
        
        double avgConfidence = confidenceScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        int highConf = (int) confidenceScores.stream().filter(score -> score >= 0.8).count();
        int mediumConf = (int) confidenceScores.stream().filter(score -> score >= 0.6 && score < 0.8).count();
        int lowConf = (int) confidenceScores.stream().filter(score -> score < 0.6).count();
        
        int totalConf = confidenceScores.size();
        
        return CampaignAnalyticsDTO.ConfidenceScores.builder()
            .averageConfidence(BigDecimal.valueOf(avgConfidence).setScale(4, RoundingMode.HALF_UP))
            .highConfidenceCount(highConf)
            .mediumConfidenceCount(mediumConf)
            .lowConfidenceCount(lowConf)
            .highConfidencePercentage(calculatePercentage(highConf, totalConf))
            .mediumConfidencePercentage(calculatePercentage(mediumConf, totalConf))
            .lowConfidencePercentage(calculatePercentage(lowConf, totalConf))
            .confidenceLevel(determineConfidenceLevel(avgConfidence))
            .build();
    }

    /**
     * Build insights from sentiment data
     */
    private CampaignAnalyticsDTO.CampaignInsights buildInsights(SentimentAnalysisData sentimentData, 
                                                               SentimentAnalysisSummary sentimentSummary) {
        
        List<InsightItem> keyFindingsItems = generateKeyFindings(sentimentData, sentimentSummary);
        List<InsightItem> recommendationsItems = generateRecommendations(sentimentData, sentimentSummary);
        List<InsightItem> trendsItems = generateTrends(sentimentData, sentimentSummary);
        List<InsightItem> riskFactorsItems = generateRiskFactors(sentimentData, sentimentSummary);

        // Extract only the text from InsightItem objects to create simple string arrays
        List<String> keyFindings = keyFindingsItems.stream()
            .map(InsightItem::getText)
            .collect(Collectors.toList());

        List<String> recommendations = recommendationsItems.stream()
            .map(InsightItem::getText)
            .collect(Collectors.toList());

        List<String> trends = trendsItems.stream()
            .map(InsightItem::getText)
            .collect(Collectors.toList());

        List<String> riskFactors = riskFactorsItems.stream()
            .map(InsightItem::getText)
            .collect(Collectors.toList());

        // Determine overall sentiment and confidence level
        String overallSentiment = "UNKNOWN";
        String confidenceLevel = "LOW";

        if (sentimentSummary != null && sentimentSummary.getSentimentDistribution() != null) {
            overallSentiment = determineOverallSentiment(sentimentSummary.getSentimentDistribution());
        }

        if (sentimentData != null && sentimentData.getFeedbackSentiments() != null) {
            double avgConfidence = sentimentData.getFeedbackSentiments().stream()
                .filter(fs -> fs.getConfidenceScore() != null)
                .mapToDouble(SentimentAnalysisData.FeedbackSentiment::getConfidenceScore)
                .average().orElse(0.0);
            confidenceLevel = determineConfidenceLevel(avgConfidence);
        }

        return CampaignAnalyticsDTO.CampaignInsights.builder()
            .keyFindings(keyFindings)
            .recommendations(recommendations)
            .trends(trends)
            .riskFactors(riskFactors)
            .overallSentiment(overallSentiment)
            .confidenceLevel(confidenceLevel)
            .build();
    }

    /**
     * Build additional metrics
     */
    private CampaignAnalyticsDTO.AdditionalMetrics buildAdditionalMetrics(Integer campaignId) {
        
        // Get total invited panelists
        Long totalInvited = panelisteCampagneRepository.countByCampagneIdAndStatut(
            campaignId, PanelisteCampagne.StatutParticipation.ACCEPTE) +
            panelisteCampagneRepository.countByCampagneIdAndStatut(
            campaignId, PanelisteCampagne.StatutParticipation.EN_COURS) +
            panelisteCampagneRepository.countByCampagneIdAndStatut(
            campaignId, PanelisteCampagne.StatutParticipation.COMPLETE);
        
        // Get total responses
        Long totalResponses = feedbackRepository.countByCampaignId(campaignId.longValue());
        
        // Calculate response rate
        BigDecimal responseRate = BigDecimal.ZERO;
        if (totalInvited > 0) {
            responseRate = calculatePercentage(totalResponses.intValue(), totalInvited.intValue());
        }
        
        // Get average rating
        Double avgRating = feedbackRepository.getAverageRatingByCampaignId(campaignId.longValue());
        BigDecimal averageRating = avgRating != null ? 
            BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        return CampaignAnalyticsDTO.AdditionalMetrics.builder()
            .responseRate(responseRate)
            .completionRate(responseRate) // Assuming all responses are complete
            .averageRating(averageRating)
            .totalInvited(totalInvited.intValue())
            .totalResponded(totalResponses.intValue())
            .totalCompleted(totalResponses.intValue())
            .build();
    }

    /**
     * Build keywords analysis
     */
    private List<CampaignAnalyticsDTO.KeywordAnalysis> buildKeywords(SentimentAnalysisData sentimentData, Integer maxKeywords) {
        
        if (sentimentData == null || sentimentData.getFeedbackSentiments() == null) {
            return new ArrayList<>();
        }
        
        Map<String, KeywordInfo> keywordMap = new HashMap<>();
        
        for (SentimentAnalysisData.FeedbackSentiment feedback : sentimentData.getFeedbackSentiments()) {
            if (feedback.getKeywords() != null) {
                for (String keyword : feedback.getKeywords()) {
                    KeywordInfo info = keywordMap.computeIfAbsent(keyword, k -> new KeywordInfo());
                    info.frequency++;
                    info.sentiments.add(feedback.getSentiment());
                    if (feedback.getConfidenceScore() != null) {
                        info.confidenceScores.add(feedback.getConfidenceScore());
                    }
                }
            }
        }
        
        return keywordMap.entrySet().stream()
            .map(entry -> {
                double avgConfidence = entry.getValue().confidenceScores.stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0.0);
                
                return CampaignAnalyticsDTO.KeywordAnalysis.builder()
                    .keyword(entry.getKey())
                    .frequency(entry.getValue().frequency)
                    .sentimentAssociation(determineSentimentAssociation(entry.getValue().sentiments))
                    .confidenceScore(BigDecimal.valueOf(avgConfidence).setScale(4, RoundingMode.HALF_UP))
                    .build();
            })
            .sorted((a, b) -> b.getFrequency().compareTo(a.getFrequency()))
            .limit(maxKeywords != null ? maxKeywords : 20)
            .collect(Collectors.toList());
    }

    /**
     * Build daily trends
     */
    private List<CampaignAnalyticsDTO.DailyTrend> buildDailyTrends(SentimentAnalysisData sentimentData, 
                                                                  LocalDate startDate, LocalDate endDate) {
        
        if (sentimentData == null || sentimentData.getFeedbackSentiments() == null) {
            return new ArrayList<>();
        }
        
        // Group by date
        Map<LocalDate, List<SentimentAnalysisData.FeedbackSentiment>> dailyGroups = 
            sentimentData.getFeedbackSentiments().stream()
                .filter(fs -> fs.getAnalyzedAt() != null)
                .filter(fs -> {
                    LocalDate date = fs.getAnalyzedAt().toLocalDate();
                    if (startDate != null && date.isBefore(startDate)) return false;
                    if (endDate != null && date.isAfter(endDate)) return false;
                    return true;
                })
                .collect(Collectors.groupingBy(fs -> fs.getAnalyzedAt().toLocalDate()));
        
        return dailyGroups.entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<SentimentAnalysisData.FeedbackSentiment> dayFeedbacks = entry.getValue();
                
                // Calculate sentiment counts
                Map<String, Long> sentimentCounts = dayFeedbacks.stream()
                    .collect(Collectors.groupingBy(
                        SentimentAnalysisData.FeedbackSentiment::getSentiment,
                        Collectors.counting()
                    ));
                
                // Calculate average confidence
                double avgConfidence = dayFeedbacks.stream()
                    .filter(fs -> fs.getConfidenceScore() != null)
                    .mapToDouble(SentimentAnalysisData.FeedbackSentiment::getConfidenceScore)
                    .average().orElse(0.0);
                
                return CampaignAnalyticsDTO.DailyTrend.builder()
                    .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .responseCount(dayFeedbacks.size())
                    .averageRating(BigDecimal.ZERO) // Would need to join with feedback data for ratings
                    .dominantSentiment(determineDominantSentiment(sentimentCounts))
                    .positiveCount(sentimentCounts.getOrDefault("POSITIVE", 0L).intValue())
                    .negativeCount(sentimentCounts.getOrDefault("NEGATIVE", 0L).intValue())
                    .neutralCount(sentimentCounts.getOrDefault("NEUTRAL", 0L).intValue())
                    .averageConfidence(BigDecimal.valueOf(avgConfidence).setScale(4, RoundingMode.HALF_UP))
                    .build();
            })
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toList());
    }

    // Insight generation methods
    private List<InsightItem> generateKeyFindings(SentimentAnalysisData sentimentData,
                                                                      SentimentAnalysisSummary sentimentSummary) {
        List<InsightItem> findings = new ArrayList<>();

        if (sentimentSummary != null && sentimentSummary.getSentimentDistribution() != null) {
            SentimentAnalysisSummary.SentimentDistribution dist = sentimentSummary.getSentimentDistribution();
            int total = dist.getPositive() + dist.getNegative() + dist.getNeutral();
            
            if (total > 0) {
                double positivePerc = (double) dist.getPositive() / total * 100;
                double negativePerc = (double) dist.getNegative() / total * 100;
                
                if (positivePerc > 70) {
                    findings.add(InsightItem.builder()
                        .text(String.format("Campaign shows overwhelmingly positive sentiment with %.1f%% positive responses", positivePerc))
                        .confidenceScore(BigDecimal.valueOf(0.9))
                        .priorityLevel("HIGH")
                        .sourceFeedbackCount(dist.getPositive())
                        .build());
                } else if (negativePerc > 50) {
                    findings.add(InsightItem.builder()
                        .text(String.format("Campaign shows concerning negative sentiment with %.1f%% negative responses", negativePerc))
                        .confidenceScore(BigDecimal.valueOf(0.85))
                        .priorityLevel("HIGH")
                        .sourceFeedbackCount(dist.getNegative())
                        .build());
                }
                
                if (total >= 50) {
                    findings.add(InsightItem.builder()
                        .text(String.format("Strong statistical significance with %d total responses analyzed", total))
                        .confidenceScore(BigDecimal.valueOf(0.95))
                        .priorityLevel("MEDIUM")
                        .sourceFeedbackCount(total)
                        .build());
                }
            }
        }
        
        return findings;
    }

    private List<InsightItem> generateRecommendations(SentimentAnalysisData sentimentData,
                                                                          SentimentAnalysisSummary sentimentSummary) {
        List<InsightItem> recommendations = new ArrayList<>();

        if (sentimentSummary != null && sentimentSummary.getSentimentDistribution() != null) {
            SentimentAnalysisSummary.SentimentDistribution dist = sentimentSummary.getSentimentDistribution();
            int total = dist.getPositive() + dist.getNegative() + dist.getNeutral();
            
            if (total > 0) {
                double negativePerc = (double) dist.getNegative() / total * 100;
                
                if (negativePerc > 30) {
                    recommendations.add(InsightItem.builder()
                        .text("Consider addressing the concerns raised in negative feedback to improve campaign reception")
                        .confidenceScore(BigDecimal.valueOf(0.8))
                        .priorityLevel("HIGH")
                        .sourceFeedbackCount(dist.getNegative())
                        .build());
                }
                
                if (dist.getNeutral() > dist.getPositive() && dist.getNeutral() > dist.getNegative()) {
                    recommendations.add(InsightItem.builder()
                        .text("Campaign generates mostly neutral responses. Consider more engaging content to drive stronger sentiment")
                        .confidenceScore(BigDecimal.valueOf(0.75))
                        .priorityLevel("MEDIUM")
                        .sourceFeedbackCount(dist.getNeutral())
                        .build());
                }
                
                if (total < 20) {
                    recommendations.add(InsightItem.builder()
                        .text("Increase sample size by gathering more feedback to improve analysis reliability")
                        .confidenceScore(BigDecimal.valueOf(0.9))
                        .priorityLevel("HIGH")
                        .sourceFeedbackCount(total)
                        .build());
                }
            }
        }
        
        return recommendations;
    }

    private List<InsightItem> generateTrends(SentimentAnalysisData sentimentData,
                                                                 SentimentAnalysisSummary sentimentSummary) {
        List<InsightItem> trends = new ArrayList<>();

        // Analyze confidence trends
        if (sentimentData != null && sentimentData.getFeedbackSentiments() != null) {
            List<Double> confidenceScores = sentimentData.getFeedbackSentiments().stream()
                .filter(fs -> fs.getConfidenceScore() != null)
                .map(SentimentAnalysisData.FeedbackSentiment::getConfidenceScore)
                .collect(Collectors.toList());
            
            if (!confidenceScores.isEmpty()) {
                double avgConfidence = confidenceScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                long highConfidenceCount = confidenceScores.stream().filter(score -> score >= 0.8).count();
                
                if (avgConfidence > 0.8) {
                    trends.add(InsightItem.builder()
                        .text(String.format("High confidence in sentiment analysis (%.1f%% average) indicates reliable feedback quality", avgConfidence * 100))
                        .confidenceScore(BigDecimal.valueOf(0.85))
                        .priorityLevel("MEDIUM")
                        .sourceFeedbackCount((int) highConfidenceCount)
                        .build());
                }
                
                // Analyze keyword trends
                if (sentimentSummary != null && sentimentSummary.getKeyInsights() != null && !sentimentSummary.getKeyInsights().isEmpty()) {
                    trends.add(InsightItem.builder()
                        .text("Key themes identified in feedback provide actionable insights for campaign optimization")
                        .confidenceScore(BigDecimal.valueOf(0.75))
                        .priorityLevel("MEDIUM")
                        .sourceFeedbackCount(sentimentSummary.getKeyInsights().size())
                        .build());
                }
            }
        }
        
        return trends;
    }

    private List<InsightItem> generateRiskFactors(SentimentAnalysisData sentimentData,
                                                                      SentimentAnalysisSummary sentimentSummary) {
        List<InsightItem> riskFactors = new ArrayList<>();

        if (sentimentSummary != null && sentimentSummary.getSentimentDistribution() != null) {
            SentimentAnalysisSummary.SentimentDistribution dist = sentimentSummary.getSentimentDistribution();
            int total = dist.getPositive() + dist.getNegative() + dist.getNeutral();
            
            if (total > 0) {
                double negativePerc = (double) dist.getNegative() / total * 100;
                
                if (negativePerc > 40) {
                    riskFactors.add(InsightItem.builder()
                        .text(String.format("High negative sentiment (%.1f%%) poses reputation risk and may impact campaign success", negativePerc))
                        .confidenceScore(BigDecimal.valueOf(0.9))
                        .priorityLevel("HIGH")
                        .sourceFeedbackCount(dist.getNegative())
                        .build());
                }
                
                if (total < 10) {
                    riskFactors.add(InsightItem.builder()
                        .text("Low response volume may not provide statistically significant insights")
                        .confidenceScore(BigDecimal.valueOf(0.7))
                        .priorityLevel("MEDIUM")
                        .sourceFeedbackCount(total)
                        .build());
                }
                
                // Check for confidence issues
                if (sentimentData != null && sentimentData.getFeedbackSentiments() != null) {
                    long lowConfidenceCount = sentimentData.getFeedbackSentiments().stream()
                        .filter(fs -> fs.getConfidenceScore() != null && fs.getConfidenceScore() < 0.6)
                        .count();
                    
                    double lowConfidencePerc = (double) lowConfidenceCount / total * 100;
                    if (lowConfidencePerc > 30) {
                        riskFactors.add(InsightItem.builder()
                            .text(String.format("%.1f%% of sentiment analysis has low confidence, potentially affecting reliability", lowConfidencePerc))
                            .confidenceScore(BigDecimal.valueOf(0.8))
                            .priorityLevel("MEDIUM")
                            .sourceFeedbackCount((int) lowConfidenceCount)
                            .build());
                    }
                }
            }
        }
        
        return riskFactors;
    }

    // Utility methods
    private BigDecimal calculatePercentage(int value, int total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf((double) value / total * 100).setScale(2, RoundingMode.HALF_UP);
    }

    private String determineOverallSentiment(SentimentAnalysisSummary.SentimentDistribution dist) {
        if (dist.getPositive() > dist.getNegative() && dist.getPositive() > dist.getNeutral()) {
            return "POSITIVE";
        } else if (dist.getNegative() > dist.getPositive() && dist.getNegative() > dist.getNeutral()) {
            return "NEGATIVE";
        } else if (dist.getNeutral() > dist.getPositive() && dist.getNeutral() > dist.getNegative()) {
            return "NEUTRAL";
        } else {
            return "MIXED";
        }
    }

    private String determineConfidenceLevel(double avgConfidence) {
        if (avgConfidence >= 0.8) return "HIGH";
        if (avgConfidence >= 0.6) return "MEDIUM";
        return "LOW";
    }

    private String determineSentimentAssociation(List<String> sentiments) {
        Map<String, Long> sentimentCounts = sentiments.stream()
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        
        return sentimentCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("MIXED");
    }

    private String determineDominantSentiment(Map<String, Long> sentimentCounts) {
        return sentimentCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("MIXED");
    }

    // Helper class for keyword processing
    private static class KeywordInfo {
        int frequency = 0;
        List<String> sentiments = new ArrayList<>();
        List<Double> confidenceScores = new ArrayList<>();
    }

    // Helper class for insight generation (used internally only)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class InsightItem {
        private String text;
        private BigDecimal confidenceScore;
        private String priorityLevel;
        private Integer sourceFeedbackCount;
    }
}
