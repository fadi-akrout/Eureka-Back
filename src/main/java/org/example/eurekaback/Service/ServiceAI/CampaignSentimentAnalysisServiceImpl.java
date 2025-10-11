package org.example.eurekaback.Service.ServiceAI;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.*;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Feedback;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CampaignSentimentAnalysisServiceImpl implements CampaignSentimentAnalysisService {
    
    private final CampagneRepository campagneRepository;
    private final FeedbackRepository feedbackRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final SentimentAnalysisPersistenceService sentimentAnalysisPersistenceService;
    
    @Override
    @Transactional
    public SaveSentimentAnalysisResponseDTO analyzeCampaignFeedbacks(Integer campaignId) {
        try {
            log.info("Starting sentiment analysis for all feedbacks of campaign ID: {}", campaignId);
            
            // Check if campaign exists
            Optional<Campagne> campagneOpt = campagneRepository.findById(campaignId);
            if (campagneOpt.isEmpty()) {
                String errorMsg = "Campaign not found with ID: " + campaignId;
                log.error(errorMsg);
                return new SaveSentimentAnalysisResponseDTO(errorMsg);
            }
            
            Campagne campagne = campagneOpt.get();
            List<Feedback> feedbacks = campagne.getFeedbacks();
            
            if (feedbacks.isEmpty()) {
                String errorMsg = "No feedbacks found for campaign ID: " + campaignId;
                log.warn(errorMsg);
                return new SaveSentimentAnalysisResponseDTO(errorMsg);
            }
            
            return analyzeFeedbacksList(campaignId, feedbacks);
            
        } catch (Exception e) {
            log.error("Error analyzing campaign feedbacks for campaign ID: {}", campaignId, e);
            return new SaveSentimentAnalysisResponseDTO("Internal server error: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SaveSentimentAnalysisResponseDTO analyzeFeedbacksBatch(Integer campaignId, List<Long> feedbackIds) {
        try {
            log.info("Starting sentiment analysis for specific feedbacks of campaign ID: {}", campaignId);
            
            // Fetch feedbacks
            List<Feedback> feedbacks = feedbackRepository.findAllById(feedbackIds);
            
            // Validate all feedbacks belong to the campaign
            boolean allBelongToCampaign = feedbacks.stream()
                .allMatch(feedback -> feedback.getCampagne().getId() == campaignId.intValue());
            
            if (!allBelongToCampaign) {
                String errorMsg = "Some feedbacks do not belong to campaign ID: " + campaignId;
                log.error(errorMsg);
                return new SaveSentimentAnalysisResponseDTO(errorMsg);
            }
            
            return analyzeFeedbacksList(campaignId, feedbacks);
            
        } catch (Exception e) {
            log.error("Error analyzing feedback batch for campaign ID: {}", campaignId, e);
            return new SaveSentimentAnalysisResponseDTO("Internal server error: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SaveSentimentAnalysisResponseDTO reAnalyzeCampaignFeedbacks(Integer campaignId) {
        log.info("Re-analyzing campaign feedbacks for campaign ID: {}", campaignId);
        
        // Delete existing analysis first
        sentimentAnalysisPersistenceService.deleteSentimentAnalysis(campaignId);
        
        // Perform new analysis
        return analyzeCampaignFeedbacks(campaignId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CampaignSentimentAnalysisResponseDTO getCampaignSentimentReport(Integer campaignId) {
        return sentimentAnalysisPersistenceService.getCampaignSentimentAnalysis(campaignId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasFeedbacksForAnalysis(Integer campaignId) {
        try {
            Optional<Campagne> campagneOpt = campagneRepository.findById(campaignId);
            if (campagneOpt.isEmpty()) {
                return false;
            }
            
            Campagne campagne = campagneOpt.get();
            return !campagne.getFeedbacks().isEmpty();
            
        } catch (Exception e) {
            log.error("Error checking feedbacks for analysis for campaign ID: {}", campaignId, e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getAnalyzableFeedbacksCount(Integer campaignId) {
        try {
            Optional<Campagne> campagneOpt = campagneRepository.findById(campaignId);
            if (campagneOpt.isEmpty()) {
                return 0;
            }
            
            Campagne campagne = campagneOpt.get();
            return campagne.getFeedbacks().size();
            
        } catch (Exception e) {
            log.error("Error getting analyzable feedbacks count for campaign ID: {}", campaignId, e);
            return 0;
        }
    }
    
    /**
     * Helper class to hold analysis results
     */
    private static class AnalysisResultsHolder {
        SentimentAnalysisData sentimentData;
        SentimentAnalysisSummary summary;
        int successfulAnalyses;
        int failedAnalyses;
        double totalConfidence;
        Map<String, Integer> sentimentCounts;
        List<String> allKeywords;
    }
    
    /**
     * Perform analysis only without saving to database
     */
    private AnalysisResultsHolder performAnalysisOnly(Integer campaignId, List<Feedback> feedbacks) {
        LocalDateTime analysisStart = LocalDateTime.now();
        
        List<SentimentAnalysisData.FeedbackSentiment> feedbackSentiments = new ArrayList<>();
        Map<String, Integer> sentimentCounts = new HashMap<>();
        sentimentCounts.put("POSITIVE", 0);
        sentimentCounts.put("NEGATIVE", 0);
        sentimentCounts.put("NEUTRAL", 0);
        
        int successfulAnalyses = 0;
        int failedAnalyses = 0;
        double totalConfidence = 0.0;
        List<String> allKeywords = new ArrayList<>();
        
        log.info("Analyzing {} feedbacks for campaign ID: {}", feedbacks.size(), campaignId);
        
        for (Feedback feedback : feedbacks) {
            try {
                // Create request for sentiment analysis
                SentimentAnalysisRequestDTO request = new SentimentAnalysisRequestDTO();
                request.setText(feedback.getCommentaire());
                
                // Analyze sentiment
                SentimentAnalysisResponseDTO response = sentimentAnalysisService.analyzeSentiment(request);
                
                if (response.isSuccess()) {
                    // Create feedback sentiment object
                    SentimentAnalysisData.FeedbackSentiment feedbackSentiment = 
                        new SentimentAnalysisData.FeedbackSentiment();
                    feedbackSentiment.setFeedbackId(feedback.getId());
                    feedbackSentiment.setSentiment(response.getSentiment());
                    feedbackSentiment.setConfidenceScore(response.getConfidenceScore());
                    
                    // Convert scores
                    SentimentAnalysisData.SentimentScores scores = new SentimentAnalysisData.SentimentScores();
                    scores.setPositive(response.getScores().getPositive());
                    scores.setNegative(response.getScores().getNegative());
                    scores.setNeutral(response.getScores().getNeutral());
                    feedbackSentiment.setScores(scores);
                    
                    feedbackSentiment.setInsights(response.getInsights());
                    feedbackSentiment.setAnalyzedAt(LocalDateTime.now());
                    feedbackSentiment.setTextLength(feedback.getCommentaire().length());
                    
                    // Simple keyword extraction
                    List<String> keywords = extractKeywords(feedback.getCommentaire());
                    feedbackSentiment.setKeywords(keywords);
                    allKeywords.addAll(keywords);
                    
                    feedbackSentiments.add(feedbackSentiment);
                    
                    // Update counts
                    sentimentCounts.put(response.getSentiment(), 
                        sentimentCounts.get(response.getSentiment()) + 1);
                    totalConfidence += response.getConfidenceScore();
                    successfulAnalyses++;
                    
                } else {
                    log.warn("Failed to analyze sentiment for feedback ID: {}", feedback.getId());
                    failedAnalyses++;
                }
                
            } catch (Exception e) {
                log.error("Error analyzing feedback ID: {}", feedback.getId(), e);
                failedAnalyses++;
            }
        }
        
        // Create batch analysis info
        SentimentAnalysisData.BatchAnalysisInfo batchInfo = new SentimentAnalysisData.BatchAnalysisInfo();
        batchInfo.setTotalBatches(1);
        batchInfo.setSuccessfulAnalyses(successfulAnalyses);
        batchInfo.setFailedAnalyses(failedAnalyses);
        batchInfo.setAverageProcessingTime("N/A");
        batchInfo.setStartTime(analysisStart);
        batchInfo.setEndTime(LocalDateTime.now());
        
        // Create sentiment analysis data
        SentimentAnalysisData sentimentData = new SentimentAnalysisData();
        sentimentData.setFeedbackSentiments(feedbackSentiments);
        sentimentData.setBatchAnalysis(batchInfo);
        
        // Create summary
        SentimentAnalysisSummary summary = createSummary(sentimentCounts, totalConfidence, 
            successfulAnalyses, allKeywords);
        
        // Create results holder
        AnalysisResultsHolder results = new AnalysisResultsHolder();
        results.sentimentData = sentimentData;
        results.summary = summary;
        results.successfulAnalyses = successfulAnalyses;
        results.failedAnalyses = failedAnalyses;
        results.totalConfidence = totalConfidence;
        results.sentimentCounts = sentimentCounts;
        results.allKeywords = allKeywords;
        
        return results;
    }
    
    /**
     * Private method to analyze a list of feedbacks and save results
     */
    private SaveSentimentAnalysisResponseDTO analyzeFeedbacksList(Integer campaignId, List<Feedback> feedbacks) {
        // Perform analysis
        AnalysisResultsHolder results = performAnalysisOnly(campaignId, feedbacks);
        
        if (results.successfulAnalyses == 0) {
            String errorMsg = "No feedbacks were successfully analyzed";
            log.error(errorMsg);
            return new SaveSentimentAnalysisResponseDTO(errorMsg);
        }
        
        // Save to database
        SaveSentimentAnalysisRequestDTO saveRequest = new SaveSentimentAnalysisRequestDTO();
        saveRequest.setCampaignId(campaignId);
        saveRequest.setSentimentAnalysisData(results.sentimentData);
        saveRequest.setSentimentAnalysisSummary(results.summary);
        saveRequest.setTotalFeedbacks(feedbacks.size());
        saveRequest.setAnalyzedFeedbacks(results.successfulAnalyses);
        
        SaveSentimentAnalysisResponseDTO result = sentimentAnalysisPersistenceService.saveSentimentAnalysis(saveRequest);
        
        log.info("Completed sentiment analysis for campaign ID: {}. Success: {}, Failed: {}", 
            campaignId, results.successfulAnalyses, results.failedAnalyses);
        
        return result;
    }
    
    /**
     * Create sentiment analysis summary from analysis results
     */
    private SentimentAnalysisSummary createSummary(Map<String, Integer> sentimentCounts, 
                                                  double totalConfidence, int successfulAnalyses,
                                                  List<String> allKeywords) {
        
        SentimentAnalysisSummary summary = new SentimentAnalysisSummary();
        
        // Determine overall sentiment
        String overallSentiment = sentimentCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("NEUTRAL");
        
        if (sentimentCounts.get("POSITIVE").equals(sentimentCounts.get("NEGATIVE"))) {
            overallSentiment = "MIXED";
        }
        
        summary.setOverallSentiment(overallSentiment);
        
        // Create distribution
        SentimentAnalysisSummary.SentimentDistribution distribution = 
            new SentimentAnalysisSummary.SentimentDistribution();
        distribution.setPositive(sentimentCounts.get("POSITIVE"));
        distribution.setNegative(sentimentCounts.get("NEGATIVE"));
        distribution.setNeutral(sentimentCounts.get("NEUTRAL"));
        summary.setSentimentDistribution(distribution);
        
        // Create percentages
        SentimentAnalysisSummary.SentimentPercentages percentages = 
            new SentimentAnalysisSummary.SentimentPercentages();
        double total = successfulAnalyses;
        percentages.setPositive((sentimentCounts.get("POSITIVE") / total) * 100);
        percentages.setNegative((sentimentCounts.get("NEGATIVE") / total) * 100);
        percentages.setNeutral((sentimentCounts.get("NEUTRAL") / total) * 100);
        summary.setSentimentPercentages(percentages);
        
        // Calculate average confidence
        double avgConfidence = totalConfidence / successfulAnalyses;
        summary.setAverageConfidence(avgConfidence);
        summary.setConfidenceLevel(avgConfidence > 0.8 ? "HIGH" : avgConfidence > 0.6 ? "MEDIUM" : "LOW");
        
        // Generate basic insights
        List<String> insights = generateInsights(sentimentCounts, avgConfidence, allKeywords);
        summary.setKeyInsights(insights);
        
        // Create trends (simplified)
        SentimentAnalysisSummary.SentimentTrends trends = new SentimentAnalysisSummary.SentimentTrends();
        Map<String, Long> keywordFrequency = allKeywords.stream()
            .collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        
        List<String> topKeywords = keywordFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        trends.setMostPositiveAspects(topKeywords);
        trends.setMostNegativeAspects(new ArrayList<>());
        trends.setNeutralAspects(new ArrayList<>());
        summary.setSentimentTrends(trends);
        
        // Create metadata
        SentimentAnalysisSummary.AnalysisMetadata metadata = 
            new SentimentAnalysisSummary.AnalysisMetadata();
        metadata.setAiModel("gemini-1.5-flash");
        metadata.setAnalysisDate(LocalDateTime.now());
        metadata.setProcessingTime("N/A");
        metadata.setApiVersion("v1");
        summary.setAnalysisMetadata(metadata);
        
        return summary;
    }
    
    /**
     * Simple keyword extraction
     */
    private List<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().split("\\s+"))
            .filter(word -> word.length() > 3)
            .filter(word -> !isStopWord(word))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * Simple stop words check
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "is", "in", "to", "of", "a", "that", "it", "with", "for", "as", "was", "on", "are", "you", "this", "be", "at", "by", "not", "or", "from", "have", "an", "they", "which", "one", "we", "all", "were", "can", "her", "has", "there", "what", "your", "when", "him", "my", "me", "will", "if", "no", "do", "would", "who", "so", "about", "out", "up", "time", "them");
        return stopWords.contains(word.toLowerCase());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CampaignAnalysisPreviewDTO analyzeCampaignFeedbacksPreview(Integer campaignId) {
        try {
            log.info("Starting sentiment analysis preview for campaign ID: {}", campaignId);
            
            // Check if campaign exists
            Optional<Campagne> campagneOpt = campagneRepository.findById(campaignId);
            if (campagneOpt.isEmpty()) {
                String errorMsg = "Campaign not found with ID: " + campaignId;
                log.error(errorMsg);
                return new CampaignAnalysisPreviewDTO(errorMsg);
            }
            
            Campagne campagne = campagneOpt.get();
            List<Feedback> feedbacks = campagne.getFeedbacks();
            
            if (feedbacks.isEmpty()) {
                String errorMsg = "No feedbacks found for campaign ID: " + campaignId;
                log.warn(errorMsg);
                return new CampaignAnalysisPreviewDTO(errorMsg);
            }
            
            // Perform analysis WITHOUT saving to database
            AnalysisResultsHolder results = performAnalysisOnly(campaignId, feedbacks);
            
            if (results.successfulAnalyses == 0) {
                String errorMsg = "No feedbacks were successfully analyzed";
                log.error(errorMsg);
                return new CampaignAnalysisPreviewDTO(errorMsg);
            }
            
            // Create preview DTO
            CampaignAnalysisPreviewDTO preview = new CampaignAnalysisPreviewDTO(
                campaignId, 
                results.sentimentData, 
                results.summary,
                feedbacks.size(),
                results.successfulAnalyses,
                results.failedAnalyses
            );
            
            preview.setAverageConfidenceScore(results.totalConfidence / results.successfulAnalyses);
            preview.setSentimentCounts(results.sentimentCounts);
            preview.setTopKeywords(results.allKeywords.stream()
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
            
            log.info("Completed sentiment analysis preview for campaign ID: {}. Success: {}, Failed: {}", 
                campaignId, results.successfulAnalyses, results.failedAnalyses);
            
            return preview;
            
        } catch (Exception e) {
            log.error("Error analyzing campaign feedbacks preview for campaign ID: {}", campaignId, e);
            return new CampaignAnalysisPreviewDTO("Internal server error: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public SaveSentimentAnalysisResponseDTO saveCampaignAnalysisResults(Integer campaignId, CampaignAnalysisPreviewDTO analysisResults) {
        try {
            log.info("Saving pre-analyzed sentiment results for campaign ID: {}", campaignId);
            
            if (!analysisResults.isSuccess()) {
                String errorMsg = "Cannot save failed analysis results";
                log.error(errorMsg);
                return new SaveSentimentAnalysisResponseDTO(errorMsg);
            }
            
            if (!analysisResults.getCampaignId().equals(campaignId)) {
                String errorMsg = "Campaign ID mismatch in analysis results";
                log.error(errorMsg);
                return new SaveSentimentAnalysisResponseDTO(errorMsg);
            }
            
            // Create save request from preview data
            SaveSentimentAnalysisRequestDTO saveRequest = new SaveSentimentAnalysisRequestDTO();
            saveRequest.setCampaignId(campaignId);
            saveRequest.setSentimentAnalysisData(analysisResults.getSentimentAnalysisData());
            saveRequest.setSentimentAnalysisSummary(analysisResults.getSentimentAnalysisSummary());
            saveRequest.setTotalFeedbacks(analysisResults.getTotalFeedbacks());
            saveRequest.setAnalyzedFeedbacks(analysisResults.getAnalyzedFeedbacks());
            
            // Save to database
            SaveSentimentAnalysisResponseDTO result = sentimentAnalysisPersistenceService.saveSentimentAnalysis(saveRequest);
            
            log.info("Successfully saved pre-analyzed results for campaign ID: {}", campaignId);
            return result;
            
        } catch (Exception e) {
            log.error("Error saving pre-analyzed results for campaign ID: {}", campaignId, e);
            return new SaveSentimentAnalysisResponseDTO("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Generate basic insights from sentiment analysis
     */
    private List<String> generateInsights(Map<String, Integer> sentimentCounts, double avgConfidence, List<String> keywords) {
        List<String> insights = new ArrayList<>();
        
        int total = sentimentCounts.values().stream().mapToInt(Integer::intValue).sum();
        double positivePercentage = (sentimentCounts.get("POSITIVE") * 100.0) / total;
        double negativePercentage = (sentimentCounts.get("NEGATIVE") * 100.0) / total;
        
        if (positivePercentage > 60) {
            insights.add("Campaign receives predominantly positive feedback from users");
        } else if (negativePercentage > 40) {
            insights.add("Campaign shows areas for improvement based on user feedback");
        } else {
            insights.add("Campaign shows mixed user sentiment with room for optimization");
        }
        
        if (avgConfidence > 0.8) {
            insights.add("High confidence in sentiment analysis results");
        } else if (avgConfidence < 0.6) {
            insights.add("Lower confidence scores suggest mixed or ambiguous feedback");
        }
        
        if (!keywords.isEmpty()) {
            insights.add("Key topics mentioned: " + String.join(", ", keywords.subList(0, Math.min(3, keywords.size()))));
        }
        
        return insights;
    }
}