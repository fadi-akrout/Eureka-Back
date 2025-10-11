package org.example.eurekaback.Controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.*;
import org.example.eurekaback.Service.ServiceAI.AITextGenerationService;
import org.example.eurekaback.Service.ServiceAI.SentimentAnalysisService;
import org.example.eurekaback.Service.ServiceAI.SentimentAnalysisPersistenceService;
import org.example.eurekaback.Service.ServiceAI.CampaignSentimentAnalysisService;
import org.example.eurekaback.Service.ServiceResultatAnalyse.ResultatAnalyseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
@Slf4j
public class AIController {
    
    private final AITextGenerationService aiTextGenerationService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final SentimentAnalysisPersistenceService sentimentAnalysisPersistenceService;
    private final CampaignSentimentAnalysisService campaignSentimentAnalysisService;
    private final ResultatAnalyseService resultatAnalyseService;

    /**
     * Generate campaign description using AI
     */
    @PostMapping("/generate-campaign-description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<AIGenerationResponseDTO> generateCampaignDescription(
            @Valid @RequestBody AIGenerationRequestDTO request) {
        
        log.info("Generating campaign description for: {}", request.getName());
        
        try {
            AIGenerationResponseDTO response = aiTextGenerationService.generateCampaignDescription(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in campaign description generation endpoint: {}", e.getMessage(), e);
            AIGenerationResponseDTO errorResponse = new AIGenerationResponseDTO(
                "Internal server error: " + e.getMessage(), true
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Generate product description using AI
     */
    @PostMapping("/generate-product-description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<AIGenerationResponseDTO> generateProductDescription(
            @Valid @RequestBody AIGenerationRequestDTO request) {
        
        log.info("Generating product description for: {}", request.getName());
        
        try {
            AIGenerationResponseDTO response = aiTextGenerationService.generateProductDescription(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in product description generation endpoint: {}", e.getMessage(), e);
            AIGenerationResponseDTO errorResponse = new AIGenerationResponseDTO(
                "Internal server error: " + e.getMessage(), true
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Analyze sentiment of campaign feedback text
     */
    @PostMapping("/analyze-sentiment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<SentimentAnalysisResponseDTO> analyzeSentiment(
            @Valid @RequestBody SentimentAnalysisRequestDTO request) {
        
        log.info("Analyzing sentiment for text with length: {}", request.getText().length());
        
        try {
            SentimentAnalysisResponseDTO response = sentimentAnalysisService.analyzeSentiment(request);
            
            if (response.isSuccess()) {
                log.info("Sentiment analysis completed: {} (confidence: {})", 
                        response.getSentiment(), response.getConfidenceScore());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in sentiment analysis endpoint: {}", e.getMessage(), e);
            SentimentAnalysisResponseDTO errorResponse = new SentimentAnalysisResponseDTO(
                "Internal server error: " + e.getMessage(), true
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Save sentiment analysis results to database
     */
    @PostMapping("/save-sentiment-analysis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<SaveSentimentAnalysisResponseDTO> saveSentimentAnalysis(
            @Valid @RequestBody SaveSentimentAnalysisRequestDTO request) {
        
        log.info("Saving sentiment analysis for campaign ID: {}", request.getCampaignId());
        
        try {
            SaveSentimentAnalysisResponseDTO response = sentimentAnalysisPersistenceService.saveSentimentAnalysis(request);
            
            if (response.isSuccess()) {
                log.info("Successfully saved sentiment analysis for campaign ID: {}", request.getCampaignId());
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to save sentiment analysis: {}", response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in save sentiment analysis endpoint: {}", e.getMessage(), e);
            SaveSentimentAnalysisResponseDTO errorResponse = new SaveSentimentAnalysisResponseDTO(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Retrieve sentiment analysis results for a campaign
     */
    @GetMapping("/campaign/{id}/sentiment-analysis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<CampaignSentimentAnalysisResponseDTO> getCampaignSentimentAnalysis(
            @PathVariable("id") Integer campaignId) {
        
        log.info("Retrieving sentiment analysis for campaign ID: {}", campaignId);
        
        try {
            CampaignSentimentAnalysisResponseDTO response = 
                sentimentAnalysisPersistenceService.getCampaignSentimentAnalysis(campaignId);
            
            if (response.isSuccess()) {
                log.info("Successfully retrieved sentiment analysis for campaign ID: {}", campaignId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("No sentiment analysis found for campaign ID: {}", campaignId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error retrieving sentiment analysis for campaign ID: {}", campaignId, e);
            CampaignSentimentAnalysisResponseDTO errorResponse = new CampaignSentimentAnalysisResponseDTO(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Check if sentiment analysis exists for a campaign
     */
    @GetMapping("/campaign/{id}/sentiment-analysis/exists")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<Map<String, Object>> checkSentimentAnalysisExists(
            @PathVariable("id") Integer campaignId) {
        
        log.info("Checking sentiment analysis existence for campaign ID: {}", campaignId);
        
        try {
            boolean exists = sentimentAnalysisPersistenceService.hasSentimentAnalysis(campaignId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("campaignId", campaignId);
            response.put("hasSentimentAnalysis", exists);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Sentiment analysis exists for campaign ID {}: {}", campaignId, exists);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking sentiment analysis existence for campaign ID: {}", campaignId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            errorResponse.put("campaignId", campaignId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete sentiment analysis for a campaign
     */
    @DeleteMapping("/campaign/{id}/sentiment-analysis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<Map<String, Object>> deleteSentimentAnalysis(
            @PathVariable("id") Integer campaignId) {
        
        log.info("Deleting sentiment analysis for campaign ID: {}", campaignId);
        
        try {
            boolean deleted = sentimentAnalysisPersistenceService.deleteSentimentAnalysis(campaignId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("campaignId", campaignId);
            response.put("message", deleted ? "Sentiment analysis deleted successfully" : "No sentiment analysis found to delete");
            response.put("timestamp", System.currentTimeMillis());
            
            if (deleted) {
                log.info("Successfully deleted sentiment analysis for campaign ID: {}", campaignId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("No sentiment analysis found to delete for campaign ID: {}", campaignId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error deleting sentiment analysis for campaign ID: {}", campaignId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            errorResponse.put("campaignId", campaignId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Analyze all feedbacks for a campaign and save sentiment results
     */
    @PostMapping("/campaign/{id}/analyze-all-feedbacks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<SaveSentimentAnalysisResponseDTO> analyzeCampaignFeedbacks(
            @PathVariable("id") Integer campaignId) {
        
        log.info("Starting full sentiment analysis for campaign ID: {}", campaignId);
        
        try {
            SaveSentimentAnalysisResponseDTO response = 
                campaignSentimentAnalysisService.analyzeCampaignFeedbacks(campaignId);
            
            if (response.isSuccess()) {
                log.info("Successfully completed sentiment analysis for campaign ID: {}", campaignId);
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to analyze campaign feedbacks: {}", response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in campaign feedback analysis endpoint: {}", e.getMessage(), e);
            SaveSentimentAnalysisResponseDTO errorResponse = new SaveSentimentAnalysisResponseDTO(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Re-analyze all feedbacks for a campaign (refresh analysis)
     */
    @PostMapping("/campaign/{id}/reanalyze-feedbacks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<SaveSentimentAnalysisResponseDTO> reAnalyzeCampaignFeedbacks(
            @PathVariable("id") Integer campaignId) {
        
        log.info("Re-analyzing sentiment for campaign ID: {}", campaignId);
        
        try {
            SaveSentimentAnalysisResponseDTO response = 
                campaignSentimentAnalysisService.reAnalyzeCampaignFeedbacks(campaignId);
            
            if (response.isSuccess()) {
                log.info("Successfully re-analyzed sentiment for campaign ID: {}", campaignId);
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to re-analyze campaign feedbacks: {}", response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in campaign feedback re-analysis endpoint: {}", e.getMessage(), e);
            SaveSentimentAnalysisResponseDTO errorResponse = new SaveSentimentAnalysisResponseDTO(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get feedback analysis readiness info for a campaign
     */
    @GetMapping("/campaign/{id}/analysis-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<Map<String, Object>> getCampaignAnalysisInfo(
            @PathVariable("id") Integer campaignId) {
        
        log.info("Getting analysis info for campaign ID: {}", campaignId);
        
        try {
            boolean hasFeedbacks = campaignSentimentAnalysisService.hasFeedbacksForAnalysis(campaignId);
            int feedbackCount = campaignSentimentAnalysisService.getAnalyzableFeedbacksCount(campaignId);
            boolean hasExistingAnalysis = sentimentAnalysisPersistenceService.hasSentimentAnalysis(campaignId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("campaignId", campaignId);
            response.put("hasFeedbacks", hasFeedbacks);
            response.put("feedbackCount", feedbackCount);
            response.put("hasExistingAnalysis", hasExistingAnalysis);
            response.put("canAnalyze", hasFeedbacks && feedbackCount > 0);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Analysis info for campaign ID {}: {} feedbacks, existing analysis: {}", 
                campaignId, feedbackCount, hasExistingAnalysis);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting analysis info for campaign ID: {}", campaignId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            errorResponse.put("campaignId", campaignId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Analyze campaign feedbacks without saving to database (preview mode)
     */
    @PostMapping("/campaign/{id}/analyze-preview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<CampaignAnalysisPreviewDTO> analyzeCampaignFeedbacksPreview(
            @PathVariable("id") Integer campaignId) {
        
        log.info("Starting sentiment analysis preview for campaign ID: {}", campaignId);
        
        try {
            CampaignAnalysisPreviewDTO response = 
                campaignSentimentAnalysisService.analyzeCampaignFeedbacksPreview(campaignId);
            
            if (response.isSuccess()) {
                log.info("Successfully completed sentiment analysis preview for campaign ID: {}", campaignId);
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to analyze campaign feedbacks preview: {}", response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in campaign feedback analysis preview endpoint: {}", e.getMessage(), e);
            CampaignAnalysisPreviewDTO errorResponse = new CampaignAnalysisPreviewDTO(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Save pre-analyzed sentiment results to database
     */
    @PostMapping("/campaign/{id}/save-analysis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<SaveSentimentAnalysisResponseDTO> saveCampaignAnalysisResults(
            @PathVariable("id") Integer campaignId,
            @RequestBody CampaignAnalysisPreviewDTO analysisResults) {
        
        log.info("Saving pre-analyzed sentiment results for campaign ID: {}", campaignId);
        
        try {
            SaveSentimentAnalysisResponseDTO response = 
                campaignSentimentAnalysisService.saveCampaignAnalysisResults(campaignId, analysisResults);
            
            if (response.isSuccess()) {
                log.info("Successfully saved pre-analyzed results for campaign ID: {}", campaignId);
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to save pre-analyzed results: {}", response.getErrorMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in save analysis results endpoint: {}", e.getMessage(), e);
            SaveSentimentAnalysisResponseDTO errorResponse = new SaveSentimentAnalysisResponseDTO(
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint for AI services
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        boolean textGenAvailable = aiTextGenerationService.isServiceAvailable();
        boolean sentimentAvailable = sentimentAnalysisService.isServiceAvailable();
        boolean overallStatus = textGenAvailable && sentimentAvailable;
        
        health.put("status", overallStatus ? "UP" : "DOWN");
        health.put("services", Map.of(
            "textGeneration", textGenAvailable ? "UP" : "DOWN",
            "sentimentAnalysis", sentimentAvailable ? "UP" : "DOWN"
        ));
        health.put("provider", "Gemini AI");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint pour récupérer l'analyse de sentiments brute depuis la table resultatanalyse
     */
    @GetMapping("/campaign/{id}/sentiment-analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<SentimentAnalyticsDTO> getSentimentAnalyticsByCampaignId(@PathVariable("id") Integer campaignId) {
        log.info("Récupération de l'analyse de sentiments brute pour la campagne ID: {}", campaignId);
        SentimentAnalyticsDTO dto = resultatAnalyseService.getSentimentAnalyticsByCampaignId(campaignId);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

