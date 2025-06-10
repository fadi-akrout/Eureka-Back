package org.example.eurekaback.Service;

import org.example.eurekaback.Dto.UserWithCampaignsDTO;
import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserCampaignService {
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private PanelisteRepository panelisteRepository;
    
    @Autowired
    private AnnonceurRepository annonceurRepository;
    
    @Transactional(readOnly = true)
    public List<UserWithCampaignsDTO> getUsersWithCampaigns() {
        List<Utilisateur> allUsers = utilisateurRepository.findAll();
        
        return allUsers.stream().map(user -> {
            UserWithCampaignsDTO dto = new UserWithCampaignsDTO();
            dto.setId((long) user.getId());
            dto.setNom(user.getNom());
            dto.setEmail(user.getEmail());
            dto.setUserType(user.getRole());
            dto.setDateInscription(user.getDateInscription());
            
            // Initialize campaign counts
            dto.setCampaignsParticipated(0);
            dto.setCampaignsCreated(0);
            
            // Set campaign counts based on user type
            if ("PANELISTE".equals(user.getRole())) {
                // Count only campaigns where user actually participated (ACCEPTE, EN_COURS, COMPLETE)
                Long participatedCount = panelisteRepository.countParticipatedCampaigns(user.getId());
                dto.setCampaignsParticipated(participatedCount != null ? participatedCount.intValue() : 0);
            } else if ("ANNONCEUR".equals(user.getRole())) {
                Optional<Annonceur> annonceurOpt = annonceurRepository.findByIdWithCampaigns(user.getId());
                if (annonceurOpt.isPresent()) {
                    Annonceur annonceur = annonceurOpt.get();
                    if (annonceur.getCampagnesCreees() != null) {
                        dto.setCampaignsCreated(annonceur.getCampagnesCreees().size());
                    }
                }
            }
            
            // TODO: Add lastLogin tracking if you have a login audit table
            // For now, we'll leave it null
            dto.setLastLogin(null);
            
            return dto;
        }).collect(Collectors.toList());
    }
}