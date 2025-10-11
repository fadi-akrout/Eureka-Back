package org.example.eurekaback.Service.ServiceFeedback;

import org.example.eurekaback.DTO.CampagneFeedbackEligibleDTO;
import org.example.eurekaback.DTO.FeedbackCreateDTO;
import org.example.eurekaback.DTO.FeedbackResponseDTO;
import org.example.eurekaback.Dto.FeedbackStatsDTO;
import org.example.eurekaback.Entity.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {
    List<Feedback> getAllFeedbacks();
    Optional<Feedback> getFeedbackById(Long id);
    Feedback createFeedback(Feedback feedback);
    void deleteFeedback(Long id);

    List<Feedback> getFeedbacksByCampagne(Long campagneId);
    double getNoteMoyennePourCampagne(Long campagneId);
    
    // New methods for enhanced functionality
    Optional<Feedback> getFeedbackByPanelistAndCampaign(Long campaignId, Long panelisteId);
    boolean hasPanelistProvidedFeedback(Long campaignId, Long panelisteId);
    List<Feedback> getFeedbacksByPanelist(Long panelisteId);
    FeedbackStatsDTO getCampaignFeedbackStats(Long campaignId);
    Feedback createFeedbackWithValidation(Feedback feedback, Long campagneId);
    boolean hasPanelistSubmittedForm(Long campaignId, Long panelisteId);
    
    // New methods for panelist feedback functionality
    List<CampagneFeedbackEligibleDTO> getEligibleCampaignsForFeedback(Long panelisteId);
    FeedbackResponseDTO createFeedbackFromDTO(FeedbackCreateDTO feedbackCreateDTO);
    List<FeedbackResponseDTO> getFeedbacksByPanelistWithDetails(Long panelisteId);
    Optional<FeedbackResponseDTO> getFeedbackByPanelistAndCampaignWithDetails(Long campaignId, Long panelisteId);
    List<FeedbackResponseDTO> getFeedbacksByCampaignWithPanelistDetails(Long campaignId);
}
