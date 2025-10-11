package org.example.eurekaback.Service.ServicePanelisteCampagne;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Entity.PanelisteCampagne.StatutParticipation;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Service.EmailService.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PanelisteCampagneServiceImpl implements PanelisteCampagneService {
    
    private static final Logger logger = LoggerFactory.getLogger(PanelisteCampagneServiceImpl.class);
    
    private final PanelisteCampagneRepository panelisteCampagneRepository;
    private final PanelisteRepository panelisteRepository;
    private final CampagneRepository campagneRepository;
    private final EmailService emailService;

    @Override
    public List<PanelisteCampagne> getAllPanelisteCampagnes() {
        return panelisteCampagneRepository.findAll();
    }

    @Override
    public Optional<PanelisteCampagne> getPanelisteCampagneById(int id) {
        return panelisteCampagneRepository.findById(id);
    }

    @Override
    public PanelisteCampagne createPanelisteCampagne(PanelisteCampagne panelisteCampagne) {
        return panelisteCampagneRepository.save(panelisteCampagne);
    }

    @Override
    public void deletePanelisteCampagne(int id) {
        panelisteCampagneRepository.deleteById(id);
    }
    
    @Override
    public Page<Paneliste> getAvailablePanelistesForCampaign(int campaignId, String search, Pageable pageable) {
        // Validate campaign exists and is active
        Campagne campagne = campagneRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        
        // Check if campaign is active or can accept invitations
        if (!("ACTIVE".equals(campagne.getStatut()) || "PLANIFIEE".equals(campagne.getStatut()))) {
            throw new IllegalStateException("Campaign is not in a state that allows invitations");
        }
        
        // Use repository methods to find available panelists
        if (search != null && !search.trim().isEmpty()) {
            return panelisteRepository.findAvailablePanelistsForCampaignWithSearch(campaignId, search, pageable);
        } else {
            return panelisteRepository.findAvailablePanelistsForCampaign(campaignId, pageable);
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> invitePanelistesToCampaign(int campaignId, List<Integer> panelisteIds) {
        // Validate campaign exists and is active
        Campagne campagne = campagneRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        
        // Check if campaign is active or can accept invitations
        if (!("ACTIVE".equals(campagne.getStatut()) || "PLANIFIEE".equals(campagne.getStatut()))) {
            throw new IllegalStateException("Campaign is not in a state that allows invitations");
        }
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> invitedPanelists = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        
        for (Integer panelisteId : panelisteIds) {
            try {
                Paneliste paneliste = panelisteRepository.findById(panelisteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Panelist not found with id: " + panelisteId));
                
                // Check if already has a relationship
                Optional<PanelisteCampagne> existingRelation = 
                        panelisteCampagneRepository.findByCampagneIdAndPanelisteId(campaignId, panelisteId);
                
                if (existingRelation.isPresent()) {
                    PanelisteCampagne relation = existingRelation.get();
                    
                    // If already invited or participating, add to errors
                    if (relation.getStatut() != StatutParticipation.REFUSE) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("panelisteId", panelisteId);
                        error.put("reason", "Already invited to this campaign with status: " + relation.getStatut());
                        errors.add(error);
                        continue;
                    }
                    
                    // If previously refused, can be invited again
                    relation.setStatut(StatutParticipation.INVITE);
                    relation.setDateInvitation(LocalDateTime.now());
                    relation.setDateReponse(null);
                    panelisteCampagneRepository.save(relation);
                    
                    Map<String, Object> invited = new HashMap<>();
                    invited.put("id", paneliste.getId());
                    invited.put("nom", paneliste.getNom());
                    invited.put("email", paneliste.getEmail());
                    invited.put("status", relation.getStatut().toString());
                    invitedPanelists.add(invited);
                    
                    // Email notification commented out per requirement
                    /*
                    String acceptUrl = "/panelistes/" + paneliste.getId() + "/invitations/" + campaignId + "/accept";
                    String rejectUrl = "/panelistes/" + paneliste.getId() + "/invitations/" + campaignId + "/reject";
                    
                    emailService.sendCampagneInvitationEmail(
                        paneliste.getEmail(),
                        "Invitation to participate in campaign: " + campagne.getNom(),
                        paneliste.getNom(),
                        campagne.getNom(),
                        campagne.getDescription(),
                        acceptUrl,
                        rejectUrl
                    );
                    */
                    
                    continue;
                }
                
                // Create new relation
                PanelisteCampagne newRelation = new PanelisteCampagne();
                newRelation.setCampagne(campagne);
                newRelation.setPaneliste(paneliste);
                newRelation.setStatut(StatutParticipation.INVITE);
                newRelation.setDateInvitation(LocalDateTime.now());
                panelisteCampagneRepository.save(newRelation);
                
                // Add to invited panelists
                Map<String, Object> invited = new HashMap<>();
                invited.put("id", paneliste.getId());
                invited.put("nom", paneliste.getNom());
                invited.put("email", paneliste.getEmail());
                invited.put("status", newRelation.getStatut().toString());
                invitedPanelists.add(invited);
                
                // Email notification commented out per requirement
                /*
                String acceptUrl = "/panelistes/" + paneliste.getId() + "/invitations/" + campaignId + "/accept";
                String rejectUrl = "/panelistes/" + paneliste.getId() + "/invitations/" + campaignId + "/reject";
                
                emailService.sendCampagneInvitationEmail(
                    paneliste.getEmail(),
                    "Invitation to participate in campaign: " + campagne.getNom(),
                    paneliste.getNom(),
                    campagne.getNom(),
                    campagne.getDescription(),
                    acceptUrl,
                    rejectUrl
                );
                */
                
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("panelisteId", panelisteId);
                error.put("reason", e.getMessage());
                errors.add(error);
            }
        }
        
        // Prepare result
        result.put("success", errors.isEmpty());
        result.put("message", invitedPanelists.size() + " panelists invited successfully");
        result.put("invitedPanelists", invitedPanelists);
        
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }
        
        return result;
    }
    
    @Override
    public List<PanelisteCampagne> getInvitationsForPaneliste(int panelisteId) {
        logger.info("Getting invitations for paneliste ID: {}", panelisteId);
        
        // Validate panelist exists
        Paneliste paneliste = panelisteRepository.findById(panelisteId)
                .orElseThrow(() -> {
                    logger.error("Paneliste with ID {} does not exist", panelisteId);
                    return new ResourceNotFoundException("Panelist not found with id: " + panelisteId);
                });
        
        // Get all relations with INVITE status
        List<PanelisteCampagne> invitations = panelisteCampagneRepository.findByPanelisteAndStatut(paneliste, StatutParticipation.INVITE);
        logger.info("Found {} invitations for paneliste {}", invitations.size(), panelisteId);
        
        if (invitations.isEmpty()) {
            logger.warn("No invitations found for paneliste {}", panelisteId);
            return new ArrayList<>();
        }
        
        // Debug each invitation
        invitations.forEach(invitation -> {
            logger.debug("Processing invitation: ID={}, Status={}, Campaign={}", 
                        invitation.getId(), invitation.getStatut(), 
                        invitation.getCampagne() != null ? invitation.getCampagne().getNom() : "NULL");
            
            if (invitation.getCampagne() == null) {
                logger.error("Campaign is null for invitation ID: {}", invitation.getId());
            }
        });
        
        return invitations;
    }
    
    @Override
    @Transactional
    public PanelisteCampagne acceptInvitation(int panelisteId, int campaignId) {
        // Find the relation
        PanelisteCampagne relation = panelisteCampagneRepository.findByCampagneIdAndPanelisteId(campaignId, panelisteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No invitation found for panelist " + panelisteId + " and campaign " + campaignId));
        
        // Validate it's an invitation
        if (relation.getStatut() != StatutParticipation.INVITE) {
            throw new IllegalStateException("Cannot accept: current status is " + relation.getStatut());
        }
        
        // Update status
        relation.setStatut(StatutParticipation.ACCEPTE);
        relation.setDateReponse(LocalDateTime.now());
        
        return panelisteCampagneRepository.save(relation);
    }
    
    @Override
    @Transactional
    public PanelisteCampagne rejectInvitation(int panelisteId, int campaignId) {
        // Find the relation
        PanelisteCampagne relation = panelisteCampagneRepository.findByCampagneIdAndPanelisteId(campaignId, panelisteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No invitation found for panelist " + panelisteId + " and campaign " + campaignId));
        
        // Validate it's an invitation
        if (relation.getStatut() != StatutParticipation.INVITE) {
            throw new IllegalStateException("Cannot reject: current status is " + relation.getStatut());
        }
        
        // Update status
        relation.setStatut(StatutParticipation.REFUSE);
        relation.setDateReponse(LocalDateTime.now());
        
        return panelisteCampagneRepository.save(relation);
    }
    
    @Override
    public Optional<PanelisteCampagne> getParticipationStatus(int panelisteId, int campaignId) {
        return panelisteCampagneRepository.findByCampagneIdAndPanelisteId(campaignId, panelisteId);
    }
    
    @Override
    @Transactional
    public PanelisteCampagne updateParticipationStatus(int panelisteId, int campaignId, StatutParticipation status) {
        // Find the relation
        PanelisteCampagne relation = panelisteCampagneRepository.findByCampagneIdAndPanelisteId(campaignId, panelisteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No relationship found for panelist " + panelisteId + " and campaign " + campaignId));
        
        // Update status
        relation.setStatut(status);
        
        // Set appropriate dates based on status
        switch (status) {
            case ACCEPTE:
                relation.setDateReponse(LocalDateTime.now());
                break;
            case REFUSE:
                relation.setDateReponse(LocalDateTime.now());
                break;
            case EN_COURS:
                // When campaign starts for a panelist
                break;
            case COMPLETE:
                relation.setDateCompletion(LocalDateTime.now());
                break;
            default:
                // No specific date to set for other statuses
                break;
        }
        
        return panelisteCampagneRepository.save(relation);
    }
}
