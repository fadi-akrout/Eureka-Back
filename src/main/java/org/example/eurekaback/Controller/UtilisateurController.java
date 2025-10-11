package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Dto.UserStatsDTO;
import org.example.eurekaback.Dto.UserWithCampaignsDTO;
import org.example.eurekaback.Entity.Admin;
import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.AdminRepository;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Service.ServiceUtilisateur.UtilisateurService;
import org.example.eurekaback.Service.UserCampaignService;
import org.example.eurekaback.Service.UserStatsService;
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
@RequestMapping("/api/utilisateurs")
@AllArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final UserStatsService userStatsService;
    private final UserCampaignService userCampaignService;
    private final AdminRepository adminRepository;
    private final AnnonceurRepository annonceurRepository;
    private final PanelisteRepository panelisteRepository;

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
    
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateUtilisateur(@PathVariable int id, @RequestBody Utilisateur updatedUser) {
        try {
            // Get the current user
            Utilisateur existingUser = utilisateurService.getUtilisateurById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
            
            // Check if role is being changed
            if (updatedUser.getRole() != null && !updatedUser.getRole().equals(existingUser.getRole())) {
                // Handle role transition
                handleRoleTransition(existingUser, updatedUser.getRole());
            }
            
            // Update basic fields
            if (updatedUser.getNom() != null) existingUser.setNom(updatedUser.getNom());
            if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
            // Update other fields as needed
            
            // Save the updated user
            return ResponseEntity.ok(utilisateurService.updateUtilisateur(existingUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
        }
    }

    private void handleRoleTransition(Utilisateur user, String newRole) {
        // First remove from current role tables if needed
        if ("ADMIN".equals(user.getRole())) {
            adminRepository.deleteById(user.getId());
        } else if ("ANNONCEUR".equals(user.getRole())) {
            annonceurRepository.deleteById(user.getId());
        } else if ("PANELISTE".equals(user.getRole())) {
            panelisteRepository.deleteById(user.getId());
        }
        
        // Then create entry in new role table
        if ("ADMIN".equals(newRole)) {
            Admin admin = new Admin();
            admin.setId(user.getId());
            adminRepository.save(admin);
        } else if ("ANNONCEUR".equals(newRole)) {
            Annonceur annonceur = new Annonceur();
            annonceur.setId(user.getId());
            annonceur.setEntreprise(""); // Default empty enterprise
            annonceurRepository.save(annonceur);
        } else if ("PANELISTE".equals(newRole)) {
            Paneliste paneliste = new Paneliste();
            paneliste.setId(user.getId());
            panelisteRepository.save(paneliste);
        }
        
        // Update the role in the base user
        user.setRole(newRole);
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