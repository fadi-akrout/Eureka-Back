package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;
import org.example.eurekaback.DTO.CampagneFeedbackEligibleDTO;
import org.example.eurekaback.DTO.FeedbackCreateDTO;
import org.example.eurekaback.DTO.FeedbackResponseDTO;
import org.example.eurekaback.Dto.FeedbackStatsDTO;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Feedback;
import org.example.eurekaback.Service.ServiceFeedback.FeedbackService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/feedbacks")
@AllArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        return feedbackService.getFeedbackById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody Feedback feedback) {
        return new ResponseEntity<>(feedbackService.createFeedback(feedback), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/campagne/{campagneId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByCampagne(@PathVariable Long campagneId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByCampagne(campagneId));
    }

    @GetMapping("/campagne/{campagneId}/note-moyenne")
    public ResponseEntity<Double> getNoteMoyennePourCampagne(@PathVariable Long campagneId) {
        return ResponseEntity.ok(feedbackService.getNoteMoyennePourCampagne(campagneId));
    }
    
    // New endpoints for enhanced functionality
    
    // Get feedback by panelist and campaign
    @GetMapping("/campagne/{campaignId}/paneliste/{panelisteId}")
    public ResponseEntity<Feedback> getFeedbackByPanelistAndCampaign(
            @PathVariable Long campaignId, 
            @PathVariable Long panelisteId) {
        return feedbackService.getFeedbackByPanelistAndCampaign(campaignId, panelisteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Check if panelist has provided feedback
    @GetMapping("/campagne/{campaignId}/paneliste/{panelisteId}/exists")
    public ResponseEntity<Boolean> hasPanelistProvidedFeedback(
            @PathVariable Long campaignId, 
            @PathVariable Long panelisteId) {
        boolean exists = feedbackService.hasPanelistProvidedFeedback(campaignId, panelisteId);
        return ResponseEntity.ok(exists);
    }

    // Get all feedbacks by panelist
    @GetMapping("/paneliste/{panelisteId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByPanelist(
            @PathVariable Long panelisteId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByPanelist(panelisteId));
    }

    // Get feedback statistics for campaign
    @GetMapping("/campagne/{campaignId}/stats")
    public ResponseEntity<FeedbackStatsDTO> getCampaignFeedbackStats(
            @PathVariable Long campaignId) {
        return ResponseEntity.ok(feedbackService.getCampaignFeedbackStats(campaignId));
    }

    // Create feedback with validation
    @PostMapping("/validated")
    public ResponseEntity<Feedback> createFeedbackWithValidation(
            @RequestParam Long campagneId,
            @Valid @RequestBody Feedback feedback) {
        
        // Override the campagne field with the parameter value to ensure consistency
        Campagne campagne = new Campagne();
        campagne.setId(campagneId.intValue());
        feedback.setCampagne(campagne);
        
        // Your validation logic using campagneId parameter
        Feedback createdFeedback = feedbackService.createFeedbackWithValidation(feedback, campagneId);
        return new ResponseEntity<>(createdFeedback, HttpStatus.CREATED);
    }

    /*@GetMapping("/campagne/{campagneId}/paginated")
    public ResponseEntity<Page<Feedback>> getFeedbacksByCampagnePaginated(
            @PathVariable int campagneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByCampagnePaginated(campagneId, page, size));
    }*/
    
    // ========== NEW PANELIST FEEDBACK ENDPOINTS ==========
    
    /**
     * Get campaigns eligible for feedback by a specific panelist
     * This includes campaigns where the panelist has participated and submitted forms
     */
    @GetMapping("/paneliste/{panelisteId}/eligible-campaigns")
    public ResponseEntity<List<CampagneFeedbackEligibleDTO>> getEligibleCampaignsForFeedback(
            @PathVariable Long panelisteId) {
        List<CampagneFeedbackEligibleDTO> eligibleCampaigns = feedbackService.getEligibleCampaignsForFeedback(panelisteId);
        return ResponseEntity.ok(eligibleCampaigns);
    }
    
    /**
     * Create feedback using DTO with validation
     * This is the main endpoint for panelists to submit feedback
     */
    @PostMapping("/paneliste/create")
    public ResponseEntity<FeedbackResponseDTO> createFeedbackFromDTO(
            @Valid @RequestBody FeedbackCreateDTO feedbackCreateDTO) {
        FeedbackResponseDTO createdFeedback = feedbackService.createFeedbackFromDTO(feedbackCreateDTO);
        return new ResponseEntity<>(createdFeedback, HttpStatus.CREATED);
    }
    
    /**
     * Get all feedbacks by panelist with detailed information
     */
    @GetMapping("/paneliste/{panelisteId}/detailed")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacksByPanelistWithDetails(
            @PathVariable Long panelisteId) {
        List<FeedbackResponseDTO> feedbacks = feedbackService.getFeedbacksByPanelistWithDetails(panelisteId);
        return ResponseEntity.ok(feedbacks);
    }
    
    /**
     * Get specific feedback by panelist and campaign with detailed information
     */
    @GetMapping("/paneliste/{panelisteId}/campagne/{campaignId}/detailed")
    public ResponseEntity<FeedbackResponseDTO> getFeedbackByPanelistAndCampaignWithDetails(
            @PathVariable Long panelisteId,
            @PathVariable Long campaignId) {
        return feedbackService.getFeedbackByPanelistAndCampaignWithDetails(campaignId, panelisteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all feedbacks for a specific campaign with panelist details
     * This is more efficient than getting basic feedbacks and then fetching panelist details separately
     */
    @GetMapping("/campagne/{campaignId}/detailed")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacksByCampaignWithPanelistDetails(
            @PathVariable Long campaignId) {
        List<FeedbackResponseDTO> feedbacks = feedbackService.getFeedbacksByCampaignWithPanelistDetails(campaignId);
        return ResponseEntity.ok(feedbacks);
    }
}