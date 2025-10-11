package org.example.eurekaback.Controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.UtilisateurRepository;
import org.example.eurekaback.Service.ServiceAnnonceur.AnnonceurService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/annonceurs")
@AllArgsConstructor
public class AnnonceurController {

    private final AnnonceurService annonceurService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    public ResponseEntity<List<Annonceur>> getAllAnnonceurs() {
        return ResponseEntity.ok(annonceurService.getAllAnnonceurs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Annonceur> getAnnonceurById(@PathVariable int id) {
        return annonceurService.getAnnonceurById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Annonceur not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Annonceur> createAnnonceur(@Valid @RequestBody Annonceur annonceur) {
        return new ResponseEntity<>(annonceurService.createAnnonceur(annonceur), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnonceur(@PathVariable int id) {
        annonceurService.deleteAnnonceur(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> partialUpdateAnnonceur(@PathVariable int id, @RequestBody Annonceur updatedAnnonceur) {
        try {
            // First check if the user exists in the base table
            Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
            // Check if this user is already an annonceur, if not, create the record
            Annonceur existingAnnonceur;
            try {
                existingAnnonceur = annonceurService.getAnnonceurById(id)
                    .orElseThrow(() -> new RuntimeException("Annonceur not found with id: " + id));
            } catch (RuntimeException e) {
                // User exists but is not an annonceur yet - check if they should be
                if ("ANNONCEUR".equals(utilisateur.getRole())) {
                    // Create a new annonceur record
                    existingAnnonceur = new Annonceur();
                    existingAnnonceur.setId(id);
                    existingAnnonceur.setEntreprise(""); // Default empty enterprise
                } else {
                    // User is not an annonceur by role
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "User exists but does not have ANNONCEUR role. Update role first."));
                }
            }
            
            // Update annonceur-specific fields
            if (updatedAnnonceur.getEntreprise() != null) {
                existingAnnonceur.setEntreprise(updatedAnnonceur.getEntreprise());
            }
            
            // Save the updated annonceur
            return ResponseEntity.ok(annonceurService.createAnnonceur(existingAnnonceur));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
        }
    }
}