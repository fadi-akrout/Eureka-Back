package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Dto.UserStatsDTO;
import org.example.eurekaback.Dto.UserWithCampaignsDTO;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Service.ServiceUtilisateur.UtilisateurService;
import org.example.eurekaback.Service.UserCampaignService;
import org.example.eurekaback.Service.UserStatsService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/utilisateurs")
@AllArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final UserStatsService userStatsService;
    private final UserCampaignService userCampaignService;

    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        return ResponseEntity.ok(utilisateurService.getAllUtilisateurs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable int id) {
        return utilisateurService.getUtilisateurById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur not found with id: " + id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Utilisateur> getUtilisateurByEmail(@PathVariable String email) {
        return utilisateurService.getUtilisateurByEmail(email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur not found with email: " + email));
    }

    @PostMapping
    public ResponseEntity<Utilisateur> createUtilisateur(@Valid @RequestBody Utilisateur utilisateur) {
        return new ResponseEntity<>(utilisateurService.createUtilisateur(utilisateur), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable int id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    // Admin-only endpoints for dashboard
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        UserStatsDTO stats = userStatsService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/with-campaigns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserWithCampaignsDTO>> getUsersWithCampaigns() {
        List<UserWithCampaignsDTO> usersWithCampaigns = userCampaignService.getUsersWithCampaigns();
        return ResponseEntity.ok(usersWithCampaigns);
    }
}