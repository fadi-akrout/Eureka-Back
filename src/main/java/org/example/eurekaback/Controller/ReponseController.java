package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;
import org.example.eurekaback.DTO.BatchResponseRequest;
import org.example.eurekaback.DTO.ReponseDetailDTO;
import org.example.eurekaback.Entity.Reponse;
import org.example.eurekaback.Security.CampaignSecurityService;
import org.example.eurekaback.Service.ServiceReponse.ReponseService;
import org.springframework.core.io.Resource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reponses")
@AllArgsConstructor
public class ReponseController {

    private final ReponseService reponseService;
    private final CampaignSecurityService campaignSecurityService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reponse>> getAllReponses() {
        return ResponseEntity.ok(reponseService.getAllReponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reponse> getReponseById(@PathVariable Integer id) {
        return reponseService.getReponseById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Reponse not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Reponse> createReponse(@Valid @RequestBody Reponse reponse) {
        return new ResponseEntity<>(reponseService.createReponse(reponse), HttpStatus.CREATED);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<?> submitBatchResponses(@Valid @RequestBody BatchResponseRequest request) {
        try {
            reponseService.saveBatchResponses(
                request.getResponses(), 
                request.getCampaignId(), 
                request.getPanelisteId()
            );
            return ResponseEntity.ok().body("Responses submitted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/formulaire/{formulaireId}")
    public ResponseEntity<List<Reponse>> getResponsesByFormulaireId(@PathVariable Integer formulaireId) {
        return ResponseEntity.ok(reponseService.getResponsesByFormulaireId(formulaireId));
    }
    
    @GetMapping("/paneliste/{panelisteId}")
    public ResponseEntity<List<Reponse>> getResponsesByPanelisteId(@PathVariable Integer panelisteId) {
        return ResponseEntity.ok(reponseService.getResponsesByPanelisteId(panelisteId));
    }
    
    @GetMapping("/paneliste/{panelisteId}/formulaire/{formulaireId}")
    public ResponseEntity<List<Reponse>> getResponsesByPanelisteIdAndFormulaireId(
            @PathVariable Integer panelisteId, 
            @PathVariable Integer formulaireId) {
        return ResponseEntity.ok(reponseService.getResponsesByPanelisteIdAndFormulaireId(panelisteId, formulaireId));
    }
    
    @GetMapping("/campaign/{campaignId}/paneliste/{panelisteId}")
    public ResponseEntity<List<Reponse>> getResponsesByCampaignIdAndPanelisteId(
            @PathVariable Integer campaignId, 
            @PathVariable Integer panelisteId) {
        return ResponseEntity.ok(reponseService.getResponsesByCampaignIdAndPanelisteId(campaignId, panelisteId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReponse(@PathVariable Integer id) {
        reponseService.deleteReponse(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all responses for a campaign with detailed information including panelist data
     * Access restricted to campaign owners (annonceurs) and admins
     */
    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasRole('ADMIN') or @campaignSecurity.isOwner(authentication, #campaignId)")
    public ResponseEntity<List<ReponseDetailDTO>> getResponsesByCampaign(
            @PathVariable Integer campaignId,
            Authentication authentication) {
        return ResponseEntity.ok(reponseService.getResponsesByCampaignWithDetails(campaignId));
    }
    
    /**
     * Get all responses for a specific panelist in a campaign with detailed information
     * Access restricted to campaign owners (annonceurs) and admins
     */
    @GetMapping("/campaign/{campaignId}/paneliste/{panelisteId}/details")
    @PreAuthorize("hasRole('ADMIN') or @campaignSecurity.isOwner(authentication, #campaignId)")
    public ResponseEntity<List<ReponseDetailDTO>> getResponsesByCampaignAndPanelisteWithDetails(
            @PathVariable Integer campaignId,
            @PathVariable Integer panelisteId,
            Authentication authentication) {
        return ResponseEntity.ok(reponseService.getResponsesByCampaignAndPanelisteWithDetails(campaignId, panelisteId));
    }
    
    /**
     * Export all responses for a campaign to CSV
     * Access restricted to campaign owners (annonceurs) and admins
     */
    @GetMapping("/campaign/{campaignId}/export")
    @PreAuthorize("hasRole('ADMIN') or @campaignSecurity.isOwner(authentication, #campaignId)")
    public ResponseEntity<Resource> exportResponsesToCsv(
            @PathVariable Integer campaignId,
            Authentication authentication) {
        return reponseService.exportResponsesToCsv(campaignId);
    }
}