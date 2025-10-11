package org.example.eurekaback.Service.ServiceFeedback;

import lombok.AllArgsConstructor;
import org.example.eurekaback.DTO.CampagneFeedbackEligibleDTO;
import org.example.eurekaback.DTO.FeedbackCreateDTO;
import org.example.eurekaback.DTO.FeedbackResponseDTO;
import org.example.eurekaback.Dto.FeedbackStatsDTO;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Feedback;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Entity.Produit;
import org.example.eurekaback.Exception.ConflictException;
import org.example.eurekaback.Exception.ForbiddenException;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.FeedbackRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Repository.ProduitRepository;
import org.example.eurekaback.Service.ServicePanelisteCampagne.PanelisteCampagneService;
import org.example.eurekaback.Service.ServiceReponse.ReponseService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final CampagneRepository campagneRepository;
    private final PanelisteRepository panelisteRepository;
    private final ProduitRepository produitRepository;
    private final PanelisteCampagneService panelisteCampagneService;
    private final ReponseService reponseService;

    @Override
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    @Override
    public Feedback createFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    @Override
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
    @Override
    public List<Feedback> getFeedbacksByCampagne(Long campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId.intValue())
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        return campagne.getFeedbacks();
    }

    @Override
    public double getNoteMoyennePourCampagne(Long campagneId) {
        List<Feedback> feedbacks = getFeedbacksByCampagne(campagneId);

        if (feedbacks.isEmpty()) {
            return 0.0;
        }

        double sum = feedbacks.stream()
                .mapToInt(Feedback::getNote)
                .sum();

        return sum / feedbacks.size();
    }
    
    @Override
    public Optional<Feedback> getFeedbackByPanelistAndCampaign(Long campaignId, Long panelisteId) {
        return feedbackRepository.findByCampagneIdAndPanelisteId(campaignId, panelisteId);
    }
    
    @Override
    public boolean hasPanelistProvidedFeedback(Long campaignId, Long panelisteId) {
        return feedbackRepository.existsByCampagneIdAndPanelisteId(campaignId, panelisteId);
    }
    
    @Override
    public List<Feedback> getFeedbacksByPanelist(Long panelisteId) {
        return feedbackRepository.findByPanelisteIdOrderByDateDesc(panelisteId);
    }
    
    @Override
    public FeedbackStatsDTO getCampaignFeedbackStats(Long campaignId) {
        Long totalFeedbacks = feedbackRepository.countByCampaignId(campaignId);
        Double averageRating = feedbackRepository.getAverageRatingByCampaignId(campaignId);
        
        // Get rating distribution
        List<Object[]> ratingDistributionData = feedbackRepository.getRatingDistributionByCampaignId(campaignId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        for (Object[] row : ratingDistributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }
        
        return new FeedbackStatsDTO(totalFeedbacks, averageRating, ratingDistribution);
    }
    
    @Override
    public Feedback createFeedbackWithValidation(Feedback feedback, Long campagneId) {
        // Validate that feedback has panelist information
        if (feedback.getPaneliste() == null || feedback.getPaneliste().getId() == 0) {
            throw new ForbiddenException("Panelist information is required");
        }
        
        Long panelisteId = (long) feedback.getPaneliste().getId();
        
        // Fetch the entities to ensure they exist
        Campagne campagne = campagneRepository.findById(campagneId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campagneId));
        
        Paneliste paneliste = panelisteRepository.findById(panelisteId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("Panelist not found with id: " + panelisteId));
        
        // Set the full entities in the feedback (not just IDs)
        feedback.setCampagne(campagne);
        feedback.setPaneliste(paneliste);
        
        // 1. Check if panelist is participating in the campaign
        Optional<PanelisteCampagne> participationOpt = panelisteCampagneService
                .getParticipationStatus(panelisteId.intValue(), campagneId.intValue());
        
        if (participationOpt.isEmpty() || 
            !participationOpt.get().getStatut().equals(PanelisteCampagne.StatutParticipation.ACCEPTE)) {
            throw new ForbiddenException("Panelist must be participating in the campaign");
        }
        
        // 2. Check if panelist has completed the formulaire
        boolean hasSubmittedForm = hasPanelistSubmittedForm(campagneId, panelisteId);
        
        if (!hasSubmittedForm) {
            throw new ForbiddenException("Panelist must complete the formulaire before providing feedback");
        }
        
        // 3. Check if feedback already exists
        if (hasPanelistProvidedFeedback(campagneId, panelisteId)) {
            throw new ConflictException("Feedback already exists for this panelist and campaign");
        }
        
        // 4. Set the date and save
        feedback.setDate(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }
    
    @Override
    public boolean hasPanelistSubmittedForm(Long campaignId, Long panelisteId) {
        // Check if panelist has submitted responses for this campaign
        List<org.example.eurekaback.Entity.Reponse> responses = reponseService
                .getResponsesByCampaignIdAndPanelisteId(campaignId.intValue(), panelisteId.intValue());
        return !responses.isEmpty();
    }
   /* private void updateProductAverageRating(int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        Optional<Produit> produitOpt = produitRepository.findByCampagne(campagne);
        if (produitOpt.isPresent()) {
            Produit produit = produitOpt.get();
            double averageRating = getNoteMoyennePourCampagne(campagneId);
            produit.setNoteMoyenne((float) averageRating);
            produitRepository.save(produit);
        }
    }*/
    
    @Override
    public List<CampagneFeedbackEligibleDTO> getEligibleCampaignsForFeedback(Long panelisteId) {
        // Get panelist
        Paneliste paneliste = panelisteRepository.findById(panelisteId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("Panelist not found with id: " + panelisteId));
        
        List<CampagneFeedbackEligibleDTO> eligibleCampaigns = new ArrayList<>();
        
        // Get all campaigns where panelist has accepted participation
        for (PanelisteCampagne pc : paneliste.getCampagnesParticipees()) {
            if (pc.getStatut() == PanelisteCampagne.StatutParticipation.ACCEPTE) {
                Campagne campagne = pc.getCampagne();
                Long campagneId = (long) campagne.getId();
                
                // Check if panelist has submitted form
                boolean hasSubmittedForm = hasPanelistSubmittedForm(campagneId, panelisteId);
                
                // Check if panelist has already provided feedback
                boolean hasFeedback = hasPanelistProvidedFeedback(campagneId, panelisteId);
                
                CampagneFeedbackEligibleDTO dto = new CampagneFeedbackEligibleDTO();
                dto.setCampagneId(campagneId);
                dto.setCampagneName(campagne.getNom());
                dto.setCampagneDescription(campagne.getDescription());
                dto.setDateDebut(campagne.getDateDebut());
                dto.setDateFin(campagne.getDateFin());
                dto.setStatut(campagne.getStatut());
                dto.setHasSubmittedForm(hasSubmittedForm);
                dto.setHasFeedback(hasFeedback);
                dto.setImageUrl(campagne.getImageUrl());
                
                eligibleCampaigns.add(dto);
            }
        }
        
        return eligibleCampaigns;
    }
    
    @Override
    public FeedbackResponseDTO createFeedbackFromDTO(FeedbackCreateDTO feedbackCreateDTO) {
        // Create Feedback entity from DTO
        Feedback feedback = new Feedback();
        feedback.setNote(feedbackCreateDTO.getNote());
        feedback.setCommentaire(feedbackCreateDTO.getCommentaire());
        
        // Set panelist
        Paneliste paneliste = new Paneliste();
        paneliste.setId(feedbackCreateDTO.getPanelisteId().intValue());
        feedback.setPaneliste(paneliste);
        
        // Create feedback with validation
        Feedback createdFeedback = createFeedbackWithValidation(feedback, feedbackCreateDTO.getCampagneId());
        
        // Convert to response DTO
        return convertToResponseDTO(createdFeedback);
    }
    
    @Override
    public List<FeedbackResponseDTO> getFeedbacksByPanelistWithDetails(Long panelisteId) {
        List<Feedback> feedbacks = getFeedbacksByPanelist(panelisteId);
        return feedbacks.stream()
                .map(this::convertToResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public Optional<FeedbackResponseDTO> getFeedbackByPanelistAndCampaignWithDetails(Long campaignId, Long panelisteId) {
        return getFeedbackByPanelistAndCampaign(campaignId, panelisteId)
                .map(this::convertToResponseDTO);
    }
    
    @Override
    public List<FeedbackResponseDTO> getFeedbacksByCampaignWithPanelistDetails(Long campaignId) {
        List<Feedback> feedbacks = getFeedbacksByCampagne(campaignId);
        return feedbacks.stream()
                .map(this::convertToResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private FeedbackResponseDTO convertToResponseDTO(Feedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(feedback.getId());
        dto.setNote(feedback.getNote());
        dto.setCommentaire(feedback.getCommentaire());
        dto.setDate(feedback.getDate());
        
        // Panelist information
        if (feedback.getPaneliste() != null) {
            dto.setPanelisteId((long) feedback.getPaneliste().getId());
            dto.setPanelisteName(feedback.getPaneliste().getNom());
            dto.setPanelisteEmail(feedback.getPaneliste().getEmail());
        }
        
        // Campaign information
        if (feedback.getCampagne() != null) {
            dto.setCampagneId((long) feedback.getCampagne().getId());
            dto.setCampagneName(feedback.getCampagne().getNom());
            dto.setCampagneDescription(feedback.getCampagne().getDescription());
        }
        
        return dto;
    }
}