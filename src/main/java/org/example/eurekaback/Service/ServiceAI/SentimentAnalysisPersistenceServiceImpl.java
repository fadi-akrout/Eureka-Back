package org.example.eurekaback.Service.ServiceAI;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.*;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.ResultatAnalyse;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.ResultatAnalyseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class SentimentAnalysisPersistenceServiceImpl implements SentimentAnalysisPersistenceService {
    
    private final ResultatAnalyseRepository resultatAnalyseRepository;
    private final CampagneRepository campagneRepository;
    
    @Override
    @Transactional
    public SaveSentimentAnalysisResponseDTO saveSentimentAnalysis(SaveSentimentAnalysisRequestDTO request) {
        try {
            log.info("Saving sentiment analysis for campaign ID: {}", request.getCampaignId());
            
            // Validate campaign exists
            Optional<Campagne> campagneOpt = campagneRepository.findById(request.getCampaignId());
            if (campagneOpt.isEmpty()) {
                String errorMsg = "Campaign not found with ID: " + request.getCampaignId();
                log.error(errorMsg);
                return new SaveSentimentAnalysisResponseDTO(errorMsg);
            }
            
            Campagne campagne = campagneOpt.get();
            ResultatAnalyse resultatAnalyse;
            
            // Check if ResultatAnalyse already exists for this campaign
            if (campagne.getResultatAnalyse() != null) {
                resultatAnalyse = campagne.getResultatAnalyse();
                log.info("Updating existing ResultatAnalyse with ID: {}", resultatAnalyse.getId());
            } else {
                // Create new ResultatAnalyse
                resultatAnalyse = new ResultatAnalyse();
                campagne.setResultatAnalyse(resultatAnalyse);
                log.info("Creating new ResultatAnalyse for campaign ID: {}", request.getCampaignId());
            }
            
            // Set sentiment analysis data
            resultatAnalyse.setSentimentData(request.getSentimentAnalysisData());
            resultatAnalyse.setSentimentSummary(request.getSentimentAnalysisSummary());
            resultatAnalyse.setSentimentLastUpdated(LocalDateTime.now());
            resultatAnalyse.setSentimentTotalFeedbacks(request.getTotalFeedbacks());
            resultatAnalyse.setSentimentAnalyzedFeedbacks(request.getAnalyzedFeedbacks());
            
            // Save the entity
            ResultatAnalyse savedResult = resultatAnalyseRepository.save(resultatAnalyse);
            
            // Update the campaign relationship if needed
            if (campagne.getResultatAnalyse() == null) {
                campagne.setResultatAnalyse(savedResult);
                campagneRepository.save(campagne);
            }
            
            log.info("Successfully saved sentiment analysis for campaign ID: {} with ResultatAnalyse ID: {}", 
                    request.getCampaignId(), savedResult.getId());
            
            return new SaveSentimentAnalysisResponseDTO(
                true,
                "Sentiment analysis saved successfully",
                request.getCampaignId(),
                savedResult.getId(),
                savedResult.getSentimentLastUpdated()
            );
            
        } catch (Exception e) {
            log.error("Error saving sentiment analysis for campaign ID: {}", request.getCampaignId(), e);
            return new SaveSentimentAnalysisResponseDTO("Internal server error: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CampaignSentimentAnalysisResponseDTO getCampaignSentimentAnalysis(Integer campaignId) {
        try {
            log.info("Retrieving sentiment analysis for campaign ID: {}", campaignId);
            
            // Find campaign
            Optional<Campagne> campagneOpt = campagneRepository.findById(campaignId);
            if (campagneOpt.isEmpty()) {
                String errorMsg = "Campaign not found with ID: " + campaignId;
                log.error(errorMsg);
                return new CampaignSentimentAnalysisResponseDTO(errorMsg);
            }
            
            Campagne campagne = campagneOpt.get();
            ResultatAnalyse resultatAnalyse = campagne.getResultatAnalyse();
            
            if (resultatAnalyse == null || resultatAnalyse.getSentimentData() == null) {
                String errorMsg = "No sentiment analysis found for campaign ID: " + campaignId;
                log.warn(errorMsg);
                return new CampaignSentimentAnalysisResponseDTO(errorMsg);
            }
            
            // Extract sentiment data and summary
            SentimentAnalysisData sentimentData = resultatAnalyse.getSentimentData();
            SentimentAnalysisSummary sentimentSummary = resultatAnalyse.getSentimentSummary();
            
            log.info("Successfully retrieved sentiment analysis for campaign ID: {}", campaignId);
            
            return new CampaignSentimentAnalysisResponseDTO(
                true,
                campaignId,
                sentimentData,
                sentimentSummary,
                resultatAnalyse.getSentimentLastUpdated(),
                resultatAnalyse.getSentimentTotalFeedbacks(),
                resultatAnalyse.getSentimentAnalyzedFeedbacks(),
                campagne.getNom()
            );
            
        } catch (Exception e) {
            log.error("Error retrieving sentiment analysis for campaign ID: {}", campaignId, e);
            return new CampaignSentimentAnalysisResponseDTO("Internal server error: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasSentimentAnalysis(Integer campaignId) {
        try {
            return resultatAnalyseRepository.existsSentimentAnalysisForCampaign(campaignId);
        } catch (Exception e) {
            log.error("Error checking sentiment analysis existence for campaign ID: {}", campaignId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteSentimentAnalysis(Integer campaignId) {
        try {
            log.info("Deleting sentiment analysis for campaign ID: {}", campaignId);
            
            Optional<Campagne> campagneOpt = campagneRepository.findById(campaignId);
            if (campagneOpt.isEmpty()) {
                log.error("Campaign not found with ID: {}", campaignId);
                return false;
            }
            
            Campagne campagne = campagneOpt.get();
            ResultatAnalyse resultatAnalyse = campagne.getResultatAnalyse();
            
            if (resultatAnalyse == null) {
                log.warn("No ResultatAnalyse found for campaign ID: {}", campaignId);
                return false;
            }
            
            // Clear sentiment analysis data
            resultatAnalyse.setSentimentData(null);
            resultatAnalyse.setSentimentSummary(null);
            resultatAnalyse.setSentimentLastUpdated(null);
            resultatAnalyse.setSentimentTotalFeedbacks(0);
            resultatAnalyse.setSentimentAnalyzedFeedbacks(0);
            
            resultatAnalyseRepository.save(resultatAnalyse);
            
            log.info("Successfully deleted sentiment analysis for campaign ID: {}", campaignId);
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting sentiment analysis for campaign ID: {}", campaignId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public SaveSentimentAnalysisResponseDTO updateSentimentAnalysis(SaveSentimentAnalysisRequestDTO request) {
        // For this implementation, update is the same as save (upsert behavior)
        return saveSentimentAnalysis(request);
    }
}