package org.example.eurekaback.Security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.CampagneRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("campaignSecurity")
@RequiredArgsConstructor
@Slf4j
public class CampaignSecurityService {

    private final CampagneRepository campagneRepository;
    private final AnnonceurRepository annonceurRepository;

    /**
     * Checks if the authenticated user is the owner of the campaign
     * 
     * @param authentication the current authentication object
     * @param campagneId the ID of the campaign to check
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(Authentication authentication, Integer campagneId) {
        log.debug("Checking ownership for campagneId: {} with authentication: {}", campagneId, 
                authentication != null ? authentication.getName() : "null");
        
        if (authentication == null || campagneId == null) {
            log.debug("Authentication or campagneId is null");
            return false;
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        log.debug("User email from authentication: {}", email);

        // Find the annonceur by email
        Optional<? extends Utilisateur> annonceurOpt = annonceurRepository.findByEmail(email);
        if (annonceurOpt.isEmpty()) {
            log.debug("No annonceur found with email: {}", email);
            return false;
        }

        Annonceur annonceur = (Annonceur) annonceurOpt.get();
        log.debug("Found annonceur with ID: {}", annonceur.getId());
        
        Optional<Campagne> campagneOpt = campagneRepository.findById(campagneId);
        
        if (campagneOpt.isEmpty()) {
            log.debug("No campaign found with ID: {}", campagneId);
            return false;
        }

        Campagne campagne = campagneOpt.get();
        Integer campaignOwnerId = campagne.getAnnonceur() != null ? campagne.getAnnonceur().getId() : null;
        Integer currentUserId = annonceur.getId();
        
        log.debug("Checking campaign ownership: userId={}, campaignId={}, ownerId={}", 
            currentUserId, campagneId, campaignOwnerId);
        
        // Use equals() instead of == for proper object comparison
        boolean isOwner = campaignOwnerId != null && campaignOwnerId.equals(currentUserId);
        
        log.debug("ID comparison result: {} == {} => {} (types: {}, {})", 
                 campaignOwnerId, currentUserId, isOwner,
                 campaignOwnerId != null ? campaignOwnerId.getClass().getSimpleName() : "null", 
                 currentUserId != null ? currentUserId.getClass().getSimpleName() : "null");
                 
        log.debug("Campaign {} belongs to annonceur ID: {}. Current user is annonceur ID: {}. isOwner: {}", 
                 campagneId, 
                 campaignOwnerId, 
                 currentUserId,
                 isOwner);
                 
        return isOwner;
    }
}