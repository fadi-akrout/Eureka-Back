package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.UtilisateurRepository;
import org.example.eurekaback.Service.ServicePaneliste.PanelisteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/panelistes")
@AllArgsConstructor
public class PanelisteController {

    private static final Logger logger = LoggerFactory.getLogger(PanelisteController.class);
    
    private final PanelisteService panelisteService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    public ResponseEntity<List<Paneliste>> getAllPanelistes() {
        return ResponseEntity.ok(panelisteService.getAllPanelistes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR') or (hasRole('PANELISTE') and @panelistSecurity.isOwner(authentication, #id))")
    public ResponseEntity<Paneliste> getPanelisteById(@PathVariable int id) {
        return panelisteService.getPanelisteById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Paneliste not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Paneliste> createPaneliste(@Valid @RequestBody Paneliste paneliste) {
        return new ResponseEntity<>(panelisteService.createPaneliste(paneliste), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaneliste(@PathVariable int id) {
        panelisteService.deletePaneliste(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Paneliste> updatePaneliste(@PathVariable int id, @Valid @RequestBody Paneliste paneliste) {
        return ResponseEntity.ok(panelisteService.updatePaneliste(id, paneliste));
    }
    
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> partialUpdatePaneliste(@PathVariable int id, @RequestBody Paneliste updatedPaneliste) {
        try {
            // First check if the user exists in the base table
            Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
            // Check if this user is already a paneliste, if not, create the record
            Paneliste existingPaneliste;
            try {
                existingPaneliste = panelisteService.getPanelisteById(id)
                    .orElseThrow(() -> new RuntimeException("Paneliste not found with id: " + id));
            } catch (RuntimeException e) {
                // User exists but is not a paneliste yet - check if they should be
                if ("PANELISTE".equals(utilisateur.getRole())) {
                    // Create a new paneliste record
                    existingPaneliste = new Paneliste();
                    existingPaneliste.setId(id);
                    // Set other required fields
                } else {
                    // User is not a paneliste by role
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "User exists but does not have PANELISTE role. Update role first."));
                }
            }
            
            // Update paneliste-specific fields
            if (updatedPaneliste.getNom() != null) {
                existingPaneliste.setNom(updatedPaneliste.getNom());
            }
            
            // Save the updated paneliste
            return ResponseEntity.ok(panelisteService.updatePaneliste(id, existingPaneliste));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/campagnes")
    public ResponseEntity<List<Campagne>> getCampagnesParticipees(@PathVariable int id) {
        return ResponseEntity.ok(panelisteService.getCampagnesParticipees(id));
    }

    @PostMapping("/{panelisteId}/participer/{campagneId}")
    public ResponseEntity<Void> participerACampagne(
            @PathVariable int panelisteId,
            @PathVariable int campagneId) {
        panelisteService.participerACampagne(panelisteId, campagneId);
        return ResponseEntity.noContent().build();
    }
}