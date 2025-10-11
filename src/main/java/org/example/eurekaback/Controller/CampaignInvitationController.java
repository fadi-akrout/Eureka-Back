package org.example.eurekaback.Controller;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Service.ServicePanelisteCampagne.PanelisteCampagneService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CampaignInvitationController {

    private final PanelisteCampagneService panelisteCampagneService;

    /**
     * Accept an invitation to a campaign
     * POST /api/campaigns/{campaignId}/accept-invitation
     * Only PANELISTE can accept their own invitations
     */
    @PostMapping("/campaigns/{campaignId}/accept-invitation")
    @PreAuthorize("hasRole('PANELISTE')")
    public ResponseEntity<?> acceptInvitation(@PathVariable int campaignId) {
        try {
            // Get the currently authenticated panelist
            Paneliste paneliste = getCurrentPaneliste();
            if (paneliste == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Unable to identify authenticated panelist"));
            }
            
            // Accept the invitation
            PanelisteCampagne updatedRelation = panelisteCampagneService.acceptInvitation(paneliste.getId(), campaignId);
            
            // Format response
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invitation accepted successfully",
                "status", updatedRelation.getStatut().toString(),
                "campaignId", campaignId,
                "panelisteId", paneliste.getId()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error accepting invitation: " + e.getMessage()));
        }
    }
    
    /**
     * Decline an invitation to a campaign
     * POST /api/campaigns/{campaignId}/decline-invitation
     * Only PANELISTE can decline their own invitations
     */
    @PostMapping("/campaigns/{campaignId}/decline-invitation")
    @PreAuthorize("hasRole('PANELISTE')")
    public ResponseEntity<?> declineInvitation(@PathVariable int campaignId) {
        try {
            // Get the currently authenticated panelist
            Paneliste paneliste = getCurrentPaneliste();
            if (paneliste == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Unable to identify authenticated panelist"));
            }
            
            // Reject the invitation
            PanelisteCampagne updatedRelation = panelisteCampagneService.rejectInvitation(paneliste.getId(), campaignId);
            
            // Format response
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invitation declined successfully",
                "status", updatedRelation.getStatut().toString(),
                "campaignId", campaignId,
                "panelisteId", paneliste.getId()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error declining invitation: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to get the currently authenticated panelist
     * @return The authenticated Paneliste or null if not authenticated or not a Paneliste
     */
    private Paneliste getCurrentPaneliste() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Paneliste) {
            return (Paneliste) authentication.getPrincipal();
        }
        return null;
    }
}