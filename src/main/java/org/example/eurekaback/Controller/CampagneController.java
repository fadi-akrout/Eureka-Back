package org.example.eurekaback.Controller;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.eurekaback.Dto.ParticipationStatusDTO;
import org.example.eurekaback.Entity.*;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Service.ServiceCampagne.CampagneService;
import org.example.eurekaback.Service.ServicePaneliste.PanelisteService;
import org.example.eurekaback.Service.ServiceParticipationRequest.ParticipationRequestService;
import org.example.eurekaback.Dto.ParticipationRequestDTO;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campagnes")
@AllArgsConstructor
public class CampagneController {

    private final CampagneService campagneService;
    private final PanelisteService panelisteService;
    private final ParticipationRequestService participationRequestService;
    private final CampagneRepository campagneRepository;
    private final AnnonceurRepository annonceurRepository;

    @GetMapping
    public ResponseEntity<List<Campagne>> getAllCampagnes() {
        return ResponseEntity.ok(campagneService.getAllCampagnes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campagne> getCampagneById(@PathVariable int id) {
        return campagneService.getCampagneById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Campagne> createCampagne(@Valid @RequestBody Campagne campagne) {
        // Get the currently authenticated annonceur
        Annonceur annonceur = getCurrentAnnonceur();
        if (annonceur != null) {
            // Set the annonceur for the campaign
            campagne.setAnnonceur(annonceur);
            // Also set the annonceurId for consistency
            campagne.setAnnonceurId(annonceur.getId());
        }
        
        return new ResponseEntity<>(campagneService.createCampagne(campagne), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampagne(@PathVariable int id) {
        campagneService.deleteCampagne(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/actives")
    public ResponseEntity<List<Campagne>> getCampagnesActives() {
        return ResponseEntity.ok(campagneService.getCampagnesActives());
    }

    @PostMapping("/complete")
    public ResponseEntity<Campagne> createCampagneComplete(@RequestBody CampagneCompleteRequest request) {
        // Get the currently authenticated annonceur
        Annonceur annonceur = getCurrentAnnonceur();
        if (annonceur != null) {
            // Set the annonceur for the campaign
            request.getCampagne().setAnnonceur(annonceur);
            // Also set the annonceurId for consistency
            request.getCampagne().setAnnonceurId(annonceur.getId());
        }
        
        Campagne createdCampagne = campagneService.createCampagneWithProduitAndFormulaire(
                request.getCampagne(), request.getProduit(), request.getFormulaire());

        // Fetch the complete campaign with associations
        Campagne completeCampagne = campagneRepository.findById(createdCampagne.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found"));
        
        // Ensure annonceurId is set for the response
        if (completeCampagne.getAnnonceur() != null) {
            completeCampagne.setAnnonceurId(completeCampagne.getAnnonceur().getId());
        }

        return new ResponseEntity<>(completeCampagne, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<Campagne> updateCampagneComplete(@PathVariable int id, @RequestBody CampagneCompleteRequest request) {
        // Log the update request
        System.out.println("Updating complete campaign: " + id + ", Request: " + request);
        
        // Get the existing campaign to check ownership
        Campagne existingCampagne = campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + id));
        
        // Get the currently authenticated annonceur
        Annonceur annonceur = getCurrentAnnonceur();
        if (annonceur != null) {
            // Set the annonceur for the campaign being updated
            request.getCampagne().setAnnonceur(annonceur);
            request.getCampagne().setAnnonceurId(annonceur.getId());
        } else if (existingCampagne.getAnnonceur() != null) {
            // Preserve the existing annonceur if not authenticated as an annonceur
            request.getCampagne().setAnnonceur(existingCampagne.getAnnonceur());
            request.getCampagne().setAnnonceurId(existingCampagne.getAnnonceur().getId());
        }
        
        // Update the campaign with its associated product and form
        Campagne updatedCampagne = campagneService.updateCampagneWithProduitAndFormulaire(
                id, request.getCampagne(), request.getProduit(), request.getFormulaire());
        
        // Ensure annonceurId is set in the response
        if (updatedCampagne.getAnnonceur() != null) {
            updatedCampagne.setAnnonceurId(updatedCampagne.getAnnonceur().getId());
        }
        
        return ResponseEntity.ok(updatedCampagne);
    }

    @PostMapping("/{campagneId}/panelistes")
    public ResponseEntity<Void> assignerPanelistes(
            @PathVariable int campagneId,
            @RequestBody List<Integer> panelisteIds) {

        Campagne campagne = campagneService.getCampagneById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + campagneId));

        // Convert IDs to Paneliste objects (you'll need to inject PanelisteService)
        List<Paneliste> panelistes = panelisteIds.stream()
                .map(id ->  panelisteService.getPanelisteById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Paneliste not found with id: " + id)))
                .collect(Collectors.toList());

        campagneService.assignerPanelistesACampagne(campagne, panelistes);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/demarrer")
    public ResponseEntity<Void> demarrerCampagne(@PathVariable int id) {
        campagneService.demarrerCampagne(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/terminer")
    public ResponseEntity<Void> terminerCampagne(@PathVariable int id) {
        campagneService.terminerCampagne(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/request-participation")
    @PreAuthorize("hasRole('PANELISTE')")
    public ResponseEntity<?> requestParticipation(@PathVariable int id) {
        try {
            // Get the currently authenticated panelist
            Paneliste paneliste = getCurrentPaneliste();
            if (paneliste == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Unable to identify authenticated panelist"));
            }
            
            // Use the campaign ID from the path and panelist ID from the authenticated user
            PanelisteCampagne request = participationRequestService.requestParticipation(id, paneliste.getId());
            return ResponseEntity.ok().body(Map.of("message", "Participation request submitted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error submitting participation request: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to get the currently authenticated annonceur
     * @return The authenticated Annonceur or null if not authenticated or not an Annonceur
     */
    private Annonceur getCurrentAnnonceur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Annonceur) {
            return (Annonceur) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Helper method to get the currently authenticated paneliste
     * @return The authenticated Paneliste or null if not authenticated or not a Paneliste
     */
    private Paneliste getCurrentPaneliste() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Paneliste) {
            return (Paneliste) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Get participation status for a panelist in a campaign
     * 
     * @param campaignId The ID of the campaign
     * @param panelisteId The ID of the panelist
     * @param authentication The current authentication object
     * @return The participation status or 403/404 if not allowed or not found
     */
    @GetMapping("/{campaignId}/participation-status/{panelisteId}")
    public ResponseEntity<?> getParticipationStatus(
            @PathVariable Integer campaignId,
            @PathVariable Integer panelisteId,
            Authentication authentication) {
        
        // Get authenticated user
        Object principal = authentication.getPrincipal();
        boolean isAdminOrAnnonceur = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ANNONCEUR"));
        
        // If user is a panelist, check if they're accessing their own status
        if (!isAdminOrAnnonceur && principal instanceof Paneliste) {
            Paneliste currentPaneliste = (Paneliste) principal;
            if (currentPaneliste.getId() != panelisteId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only access your own participation status"));
            }
        } else if (!isAdminOrAnnonceur) {
            // Not admin, not annonceur, and not a panelist looking at their own status
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Insufficient permissions to access this resource"));
        }
        
        try {
            Optional<ParticipationStatusDTO> status = participationRequestService.getParticipationStatus(campaignId, panelisteId);
            return status.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Error retrieving participation status: " + e.getMessage()));
        }
    }
}

@Data
class CampagneCompleteRequest {
    @NotNull
    private Campagne campagne;
    @NotNull
    private Produit produit;
    @NotNull
    private Formulaire formulaire;
}