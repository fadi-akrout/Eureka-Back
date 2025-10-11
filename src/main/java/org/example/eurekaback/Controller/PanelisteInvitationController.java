package org.example.eurekaback.Controller;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Dto.ParticipationDTO;
import org.example.eurekaback.Dto.ParticipationHistoryDTO;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Entity.PanelisteCampagne.StatutParticipation;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Service.ServicePanelisteCampagne.PanelisteCampagneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class PanelisteInvitationController {

    private final PanelisteCampagneService panelisteCampagneService;
    private final CampagneRepository campagneRepository;
    private final PanelisteRepository panelisteRepository;
    private final PanelisteCampagneRepository panelisteCampagneRepository;

    /**
     * 1. Get Panelists Available for Invitation
     * GET /api/campaigns/{campaignId}/available-panelists
     * Only ADMIN or the campaign owner (ANNONCEUR) can access this endpoint
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    @GetMapping("/campaigns/{campaignId}/available-panelists")
    public ResponseEntity<Map<String, Object>> getAvailablePanelists(
            @PathVariable int campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        // Validate campaign exists
        Campagne campagne = campagneRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        
        // Check if the current user is authorized to access this campaign
        checkCampaignAccessAuthorization(campagne);
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        
        // Get available panelists
        Page<Paneliste> panelistesPage = panelisteCampagneService.getAvailablePanelistesForCampaign(
                campaignId, search, pageable);
        
        // Format response
        Map<String, Object> response = new HashMap<>();
        
        // Transform panelists to include interests and set status to null
        List<Map<String, Object>> panelistesData = panelistesPage.getContent().stream()
                .map(paneliste -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", paneliste.getId());
                    data.put("nom", paneliste.getNom());
                    data.put("email", paneliste.getEmail());
                    data.put("interets", paneliste.getInterets());
                    data.put("status", null); // Null status as per requirement
                    return data;
                })
                .collect(Collectors.toList());
        
        response.put("content", panelistesData);
        response.put("totalElements", panelistesPage.getTotalElements());
        response.put("totalPages", panelistesPage.getTotalPages());
        response.put("currentPage", panelistesPage.getNumber());
        response.put("size", panelistesPage.getSize());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to check if the current user is authorized to access a campaign
     * Only ADMIN or the campaign owner (ANNONCEUR) can access
     */
    private void checkCampaignAccessAuthorization(Campagne campagne) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
        
        // If user is ADMIN, they have access to all campaigns
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }
        
        // If user is ANNONCEUR, check if they are the owner of the campaign
        if ("ANNONCEUR".equals(currentUser.getRole())) {
            if (campagne.getAnnonceur() != null && campagne.getAnnonceur().getId() == currentUser.getId()) {
                return;
            }
        }
        
        // If we get here, the user is not authorized
        throw new AccessDeniedException("You are not authorized to access this campaign");
    }

    /**
     * 2. Invite Panelist(s) to Campaign
     * POST /api/campaigns/{campaignId}/invite-panelists
     * Only ADMIN or the campaign owner (ANNONCEUR) can invite panelists
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    @PostMapping("/campaigns/{campaignId}/invite-panelists")
    public ResponseEntity<Map<String, Object>> invitePanelists(
            @PathVariable int campaignId,
            @RequestBody Map<String, Object> requestBody) {
        
        List<Integer> panelisteIds = extractPanelisteIds(requestBody);
        if (panelisteIds == null || panelisteIds.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "No panelists specified for invitation");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Validate campaign exists
        Campagne campagne = campagneRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        
        // Check if the current user is authorized to access this campaign
        checkCampaignAccessAuthorization(campagne);
        
        // Invite panelists
        Map<String, Object> result = panelisteCampagneService.invitePanelistesToCampaign(campaignId, panelisteIds);
        
        // Return appropriate response based on success
        HttpStatus status = (Boolean) result.getOrDefault("success", false) ? 
                HttpStatus.OK : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 3. Get Invitations for Panelist
     * GET /api/panelistes/{panelisteId}/invitations
     * ADMIN, ANNONCEUR, or the panelist themselves can view their invitations
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR') or hasRole('PANELISTE')")
    @GetMapping("/panelistes/{panelisteId}/invitations")
    public ResponseEntity<Map<String, Object>> getInvitationsForPanelist(@PathVariable int panelisteId) {
        // Validate panelist exists
        if (!panelisteRepository.existsById(panelisteId)) {
            throw new ResourceNotFoundException("Panelist not found with id: " + panelisteId);
        }
        
        // Check if current user is authorized to view this panelist's invitations
        checkPanelisteAccessAuthorization(panelisteId);
        
        // Get invitations
        List<PanelisteCampagne> invitations = panelisteCampagneService.getInvitationsForPaneliste(panelisteId);
        
        // Format response
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> invitationsData = invitations.stream()
                .map(invitation -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", invitation.getId());
                    data.put("status", invitation.getStatut().toString());
                    data.put("dateInvitation", invitation.getDateInvitation());
                    
                    // Campaign info
                    Campagne campagne = invitation.getCampagne();
                    Map<String, Object> campagneData = new HashMap<>();
                    campagneData.put("id", campagne.getId());
                    campagneData.put("nom", campagne.getNom());
                    campagneData.put("description", campagne.getDescription());
                    campagneData.put("statut", campagne.getStatut());
                    campagneData.put("dateDebut", campagne.getDateDebut());
                    campagneData.put("dateFin", campagne.getDateFin());
                    
                    // Set annonceurId
                    if (campagne.getAnnonceur() != null) {
                        campagneData.put("annonceurId", campagne.getAnnonceur().getId());
                    }
                    
                    // Add product info if available
                    if (campagne.getProduit() != null) {
                        Map<String, Object> produitData = new HashMap<>();
                        produitData.put("id", campagne.getProduit().getId());
                        produitData.put("nom", campagne.getProduit().getNom());
                        produitData.put("description", campagne.getProduit().getDescription());
                        produitData.put("categorie", campagne.getProduit().getCategorie());
                        campagneData.put("produit", produitData);
                    }
                    
                    data.put("campagne", campagneData);
                    return data;
                })
                .collect(Collectors.toList());
        
        response.put("invitations", invitationsData);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to check if the current user is authorized to access a panelist's data
     * ADMIN, ANNONCEUR, or the panelist themselves can access
     */
    private void checkPanelisteAccessAuthorization(int panelisteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
        
        // If user is ADMIN, they have access to all panelists
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }
        
        // If user is ANNONCEUR, they have access to all panelists
        if ("ANNONCEUR".equals(currentUser.getRole())) {
            return;
        }
        
        // If user is PANELISTE, check if they are accessing their own data
        if ("PANELISTE".equals(currentUser.getRole()) && currentUser.getId() == panelisteId) {
            return;
        }
        
        // If we get here, the user is not authorized
        throw new AccessDeniedException("You are not authorized to access this panelist's data");
    }
    
    /**
     * Helper method to extract panelist IDs from request body
     * Handles both single integer and array of integers
     */
    private List<Integer> extractPanelisteIds(Map<String, Object> requestBody) {
        Object panelisteIdsObj = requestBody.get("panelisteIds");
        
        if (panelisteIdsObj == null) {
            return null;
        }
        
        List<Integer> panelisteIds = new java.util.ArrayList<>();
        
        if (panelisteIdsObj instanceof List) {
            // Handle array case
            List<?> list = (List<?>) panelisteIdsObj;
            for (Object item : list) {
                if (item instanceof Integer) {
                    panelisteIds.add((Integer) item);
                } else if (item instanceof Number) {
                    panelisteIds.add(((Number) item).intValue());
                }
            }
        } else if (panelisteIdsObj instanceof Integer) {
            // Handle single integer case
            panelisteIds.add((Integer) panelisteIdsObj);
        } else if (panelisteIdsObj instanceof Number) {
            // Handle other number types
            panelisteIds.add(((Number) panelisteIdsObj).intValue());
        }
        
        return panelisteIds;
    }

    /**
     * 4. Get Participation History for Panelist
     * GET /api/panelistes/{panelisteId}/participation-history
     * ADMIN and ANNONCEUR can view any panelist's participation history
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    @GetMapping("/panelistes/{panelisteId}/participation-history")
    public ResponseEntity<ParticipationHistoryDTO> getParticipationHistory(@PathVariable int panelisteId) {
        // Validate panelist exists
        if (!panelisteRepository.existsById(panelisteId)) {
            throw new ResourceNotFoundException("Panelist not found with id: " + panelisteId);
        }
        
        // Get participation history
        List<PanelisteCampagne> participations = panelisteCampagneService.getInvitationsForPaneliste(panelisteId);
        
        // Create response DTO
        ParticipationHistoryDTO historyDTO = new ParticipationHistoryDTO();
        historyDTO.setPanelisteId((long) panelisteId);
        historyDTO.setTotalParticipations(participations.size());
        
        // Convert to participation DTOs
        List<ParticipationDTO> participationDTOs = participations.stream()
                .map(this::convertToParticipationDTO)
                .collect(Collectors.toList());
        
        historyDTO.setParticipations(participationDTOs);
        
        return ResponseEntity.ok(historyDTO);
    }
    
    /**
     * Helper method to convert PanelisteCampagne to ParticipationDTO
     */
    private ParticipationDTO convertToParticipationDTO(PanelisteCampagne pc) {
        ParticipationDTO dto = new ParticipationDTO();
        dto.setCampaignId((long) pc.getCampagne().getId());
        dto.setCampaignName(pc.getCampagne().getNom());
        dto.setCampaignStatus(pc.getCampagne().getStatut());
        dto.setCampaignStartDate(pc.getCampagne().getDateDebut());
        dto.setCampaignEndDate(pc.getCampagne().getDateFin());
        dto.setParticipationStatus(pc.getStatut());
        dto.setInvitationDate(pc.getDateInvitation());
        dto.setResponseDate(pc.getDateReponse());
        
        return dto;
    }

    /**
     * 5. Accept Campaign Invitation
     * POST /api/panelistes/{panelisteId}/invitations/{campaignId}/accept
     * Only ADMIN or the panelist themselves can accept an invitation
     */
  /*  @PreAuthorize("hasRole('ADMIN') or hasRole('PANELISTE')")
    @PostMapping("/panelistes/{panelisteId}/invitations/{campaignId}/accept")
    public ResponseEntity<Map<String, Object>> acceptInvitation(
            @PathVariable int panelisteId,
            @PathVariable int campaignId) {
        
        try {
            // Check if current user is authorized to accept this invitation
            checkPanelisteAccessAuthorization(panelisteId);
            
            // Accept invitation
            PanelisteCampagne updatedRelation = panelisteCampagneService.acceptInvitation(panelisteId, campaignId);
            
            // Format response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation accepted successfully");
            response.put("status", updatedRelation.getStatut().toString());
            response.put("campaignId", campaignId);
            response.put("panelisteId", panelisteId);
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException | AccessDeniedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }
*/
    /**
     * 5. Reject Campaign Invitation
     * POST /api/panelistes/{panelisteId}/invitations/{campaignId}/reject
     * Only ADMIN or the panelist themselves can reject an invitation
     */
  /* @PreAuthorize("hasRole('ADMIN') or hasRole('PANELISTE')")
    @PostMapping("/panelistes/{panelisteId}/invitations/{campaignId}/reject")
    public ResponseEntity<Map<String, Object>> rejectInvitation(
            @PathVariable int panelisteId,
            @PathVariable int campaignId) {
        
        try {
            // Check if current user is authorized to reject this invitation
            checkPanelisteAccessAuthorization(panelisteId);
            
            // Reject invitation
            PanelisteCampagne updatedRelation = panelisteCampagneService.rejectInvitation(panelisteId, campaignId);
            
            // Format response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation rejected successfully");
            response.put("status", updatedRelation.getStatut().toString());
            response.put("campaignId", campaignId);
            response.put("panelisteId", panelisteId);
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException | AccessDeniedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }
*/
    /**
     * Get All Panelists Status for a Campaign
     * GET /api/campagnes/{campaignId}/panelistes-status
     * ADMIN or ANNONCEUR (campaign owner) can view all panelists status for a campaign
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    @GetMapping("/campaigns/{campaignId}/panelistes-status")
    public ResponseEntity<Map<String, Object>> getAllPanelistesStatusForCampaign(
            @PathVariable int campaignId) {
        
        // Validate campaign exists
        Campagne campagne = campagneRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        
        // Check if ANNONCEUR is the owner of the campaign
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
        
        if ("ANNONCEUR".equals(currentUser.getRole())) {
            if (campagne.getAnnonceur() == null || campagne.getAnnonceur().getId() != currentUser.getId()) {
                throw new AccessDeniedException("You are not authorized to access this campaign's data");
            }
        }
        
        // Get all panelist-campaign relationships for this campaign
        List<PanelisteCampagne> relationships = panelisteCampagneRepository.findByCampagne(campagne);
        
        // Format response
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> panelistesData = relationships.stream()
                .map(relation -> {
                    Map<String, Object> data = new HashMap<>();
                    Paneliste paneliste = relation.getPaneliste();
                    
                    data.put("id", paneliste.getId());
                    data.put("nom", paneliste.getNom());
                    data.put("email", paneliste.getEmail());
                    data.put("status", relation.getStatut().toString());
                    data.put("dateInvitation", relation.getDateInvitation());
                    data.put("dateReponse", relation.getDateReponse());
                    data.put("dateCompletion", relation.getDateCompletion());
                    
                    return data;
                })
                .collect(Collectors.toList());
        
        response.put("campaignId", campaignId);
        response.put("campaignNom", campagne.getNom());
        response.put("panelistes", panelistesData);
        response.put("totalPanelistes", panelistesData.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 6. Get Panelist Participation Status
     * GET /api/campaigns/{campaignId}/panelistes/{panelisteId}/status
     * ADMIN, ANNONCEUR (campaign owner), or the PANELISTE themselves can view status
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR') or hasRole('PANELISTE')")
    @GetMapping("/campaigns/{campaignId}/panelistes/{panelisteId}/status")
    public ResponseEntity<Map<String, Object>> getParticipationStatus(
            @PathVariable int campaignId,
            @PathVariable int panelisteId) {
        
        // Validate that current user can access this information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
        
        // If not ADMIN, check if ANNONCEUR (and campaign owner) or PANELISTE (and self)
        if (!"ADMIN".equals(currentUser.getRole())) {
            // For ANNONCEUR, check if they own the campaign
            if ("ANNONCEUR".equals(currentUser.getRole())) {
                Campagne campagne = campagneRepository.findById(campaignId)
                        .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
                
                if (campagne.getAnnonceur() == null || campagne.getAnnonceur().getId() != currentUser.getId()) {
                    throw new AccessDeniedException("You are not authorized to access this campaign's data");
                }
            } 
            // For PANELISTE, check if viewing their own status
            else if ("PANELISTE".equals(currentUser.getRole())) {
                if (currentUser.getId() != panelisteId) {
                    throw new AccessDeniedException("You can only view your own participation status");
                }
            }
        }
        
        // Get participation status
        Optional<PanelisteCampagne> relationOpt = panelisteCampagneService.getParticipationStatus(panelisteId, campaignId);
        
        if (relationOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No relationship found for panelist " + panelisteId + " and campaign " + campaignId);
        }
        
        PanelisteCampagne relation = relationOpt.get();
        
        // Format response
        Map<String, Object> response = new HashMap<>();
        response.put("panelisteId", panelisteId);
        response.put("campaignId", campaignId);
        response.put("status", relation.getStatut().toString());
        response.put("dateInvitation", relation.getDateInvitation());
        response.put("dateReponse", relation.getDateReponse());
        
        return ResponseEntity.ok(response);
    }
}