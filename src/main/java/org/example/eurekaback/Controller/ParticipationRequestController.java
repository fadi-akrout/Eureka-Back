package org.example.eurekaback.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.MessageResponse;
import org.example.eurekaback.Dto.ParticipationApprovalRequest;
import org.example.eurekaback.Dto.ParticipationRequestDTO;
import org.example.eurekaback.Dto.ParticipationStatusDTO;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Service.ServiceParticipationRequest.ParticipationRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/participation-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ParticipationRequestController {
    
    private final ParticipationRequestService participationRequestService;
    
    /**
     * Submit a participation request
     */
    @PostMapping("/request")
    @PreAuthorize("hasRole('PANELISTE')")
    public ResponseEntity<?> requestParticipation(
            @RequestParam Integer campagneId,
            @RequestParam Integer panelisteId) {
        try {
            PanelisteCampagne request = participationRequestService.requestParticipation(campagneId, panelisteId);
            return ResponseEntity.ok(new MessageResponse("Participation request submitted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error submitting participation request: " + e.getMessage()));
        }
    }
    
    /**
     * Get pending participation requests for a campaign
     */
    @GetMapping("/campaign/{campagneId}/pending")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ANNONCEUR') and @campaignSecurity.isOwner(authentication, #campagneId))")
    public ResponseEntity<List<ParticipationRequestDTO>> getPendingRequests(@PathVariable Integer campagneId) {
        try {
            List<ParticipationRequestDTO> requests = participationRequestService.getPendingRequests(campagneId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Approve a participation request
     */
    @PutMapping("/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<?> approveRequest(@RequestBody ParticipationApprovalRequest request) {
        try {
            // Extract values from the request object
            Integer campagneId = request.getCampagneId();
            Integer panelisteId = request.getPanelisteId();
            Integer annonceurId = request.getAnnonceurId();
            
            // Log the received values for debugging
            log.debug("Approving request with campagneId={}, panelisteId={}, annonceurId={}",
                    campagneId, panelisteId, annonceurId);
                    
            if (campagneId == null || panelisteId == null) {
                return ResponseEntity.badRequest().body(
                    new MessageResponse("Missing required parameters: campagneId and panelisteId are required"));
            }
            
            // Manual security check for ANNONCEUR role
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && 
                authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ANNONCEUR"))) {
                
                // Get the campaignSecurity bean and check ownership
                org.springframework.web.context.request.ServletRequestAttributes attributes = 
                    (org.springframework.web.context.request.ServletRequestAttributes) 
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attributes == null) {
                    throw new IllegalStateException("No request attributes found");
                }
                
                org.springframework.web.context.WebApplicationContext webApplicationContext = 
                    org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(
                        attributes.getRequest().getServletContext());
                        
                org.example.eurekaback.Security.CampaignSecurityService campaignSecurity = 
                    webApplicationContext.getBean(org.example.eurekaback.Security.CampaignSecurityService.class);
                
                if (!campaignSecurity.isOwner(authentication, campagneId)) {
                    log.error("User {} is not owner of campaign {}", authentication.getName(), campagneId);
                    return ResponseEntity.status(403).body(
                        new MessageResponse("You don't have permission to approve requests for this campaign"));
                }
            }
            
            // Default annonceurId to 0 if not provided (system approval)
            if (annonceurId == null) annonceurId = 0;
            
            PanelisteCampagne requestResult = participationRequestService.approveRequest(
                    campagneId, panelisteId, annonceurId);
                    
            return ResponseEntity.ok(new MessageResponse("Participation request approved successfully"));
        } catch (SecurityException e) {
            log.error("Security exception when approving request: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Illegal state when approving request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error approving participation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error approving participation request: " + e.getMessage()));
        }
    }
    
    /**
     * Reject a participation request
     */
    @PutMapping("/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<?> rejectRequest(@RequestBody ParticipationApprovalRequest request) {
        try {
            // Extract values from the request object
            Integer campagneId = request.getCampagneId();
            Integer panelisteId = request.getPanelisteId();
            Integer annonceurId = request.getAnnonceurId();
            String feedback = request.getFeedback();
            
            // Log the received values for debugging
            log.debug("Rejecting request with campagneId={}, panelisteId={}, annonceurId={}, feedback={}",
                    campagneId, panelisteId, annonceurId, feedback);
                    
            if (campagneId == null || panelisteId == null) {
                return ResponseEntity.badRequest().body(
                    new MessageResponse("Missing required parameters: campagneId and panelisteId are required"));
            }
            
            // Manual security check for ANNONCEUR role
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && 
                authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ANNONCEUR"))) {
                
                // Get the campaignSecurity bean and check ownership
                org.springframework.web.context.request.ServletRequestAttributes attributes = 
                    (org.springframework.web.context.request.ServletRequestAttributes) 
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attributes == null) {
                    throw new IllegalStateException("No request attributes found");
                }
                
                org.springframework.web.context.WebApplicationContext webApplicationContext = 
                    org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(
                        attributes.getRequest().getServletContext());
                        
                org.example.eurekaback.Security.CampaignSecurityService campaignSecurity = 
                    webApplicationContext.getBean(org.example.eurekaback.Security.CampaignSecurityService.class);
                
                if (!campaignSecurity.isOwner(authentication, campagneId)) {
                    log.error("User {} is not owner of campaign {}", authentication.getName(), campagneId);
                    return ResponseEntity.status(403).body(
                        new MessageResponse("You don't have permission to reject requests for this campaign"));
                }
            }
            
            // Default annonceurId to 0 if not provided (system rejection)
            if (annonceurId == null) annonceurId = 0;
            
            PanelisteCampagne requestResult = participationRequestService.rejectRequest(
                    campagneId, panelisteId, annonceurId, feedback);
                    
            return ResponseEntity.ok(new MessageResponse("Participation request rejected successfully"));
        } catch (SecurityException e) {
            log.error("Security exception when rejecting request: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Illegal state when rejecting request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error rejecting participation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error rejecting participation request: " + e.getMessage()));
        }
    }
    
    /**
     * Get participation status for a panelist in a campaign
     */
    @GetMapping("/status")
    public ResponseEntity<ParticipationStatusDTO> getParticipationStatus(
            @RequestParam Integer campagneId,
            @RequestParam Integer panelisteId) {
        try {
            Optional<ParticipationStatusDTO> status = participationRequestService.getParticipationStatus(campagneId, panelisteId);
            return status.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cancel a pending participation request
     */
    @DeleteMapping("/cancel")
    @PreAuthorize("hasRole('PANELISTE')")
    public ResponseEntity<?> cancelRequest(
            @RequestParam Integer campagneId,
            @RequestParam Integer panelisteId) {
        try {
            participationRequestService.cancelRequest(campagneId, panelisteId);
            return ResponseEntity.ok(new MessageResponse("Participation request cancelled successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error cancelling participation request: " + e.getMessage()));
        }
    }
    
    /**
     * Get all participation requests for a panelist
     */
    @GetMapping("/panelist/{panelisteId}")
    @PreAuthorize("hasRole('PANELISTE') or hasRole('ADMIN')")
    public ResponseEntity<List<ParticipationRequestDTO>> getRequestsByPanelist(@PathVariable Integer panelisteId) {
        try {
            List<ParticipationRequestDTO> requests = participationRequestService.getRequestsByPanelist(panelisteId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Count pending requests for a campaign
     */
    @GetMapping("/campaign/{campagneId}/count")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ANNONCEUR') and @campaignSecurity.isOwner(authentication, #campagneId))")
    public ResponseEntity<Long> countPendingRequests(@PathVariable Integer campagneId) {
        try {
            long count = participationRequestService.countPendingRequests(campagneId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}