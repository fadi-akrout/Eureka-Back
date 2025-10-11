package org.example.eurekaback.Security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("panelistSecurity")
@RequiredArgsConstructor
@Slf4j
public class PanelistSecurityService {

    private final PanelisteRepository panelisteRepository;

    /**
     * Checks if the authenticated user is the panelist with the given ID
     * 
     * @param authentication the current authentication object
     * @param panelisteId the ID of the panelist to check
     * @return true if the user is the panelist, false otherwise
     */
    public boolean isOwner(Authentication authentication, Integer panelisteId) {
        log.debug("Checking panelist ownership for panelisteId: {} with authentication: {}", panelisteId, 
                authentication != null ? authentication.getName() : "null");
        
        if (authentication == null || panelisteId == null) {
            log.debug("Authentication or panelisteId is null");
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

        // Find the panelist by email
        Optional<? extends Utilisateur> panelisteOpt = panelisteRepository.findByEmail(email);
        if (panelisteOpt.isEmpty()) {
            log.debug("No panelist found with email: {}", email);
            return false;
        }

        Paneliste paneliste = (Paneliste) panelisteOpt.get();
        log.debug("Found panelist with ID: {}", paneliste.getId());
        
        Integer currentUserId = paneliste.getId();
        
        log.debug("Checking panelist ownership: userId={}, panelisteId={}", 
            currentUserId, panelisteId);
        
        // Use equals() instead of == for proper object comparison
        boolean isOwner = panelisteId.equals(currentUserId);
        
        log.debug("ID comparison result: {} == {} => {} (types: {}, {})", 
                 panelisteId, currentUserId, isOwner,
                 panelisteId.getClass().getSimpleName(), 
                 currentUserId != null ? currentUserId.getClass().getSimpleName() : "null");
                 
        log.debug("Current user is panelist ID: {}. Requested panelisteId: {}. isOwner: {}", 
                 currentUserId, 
                 panelisteId,
                 isOwner);
                 
        return isOwner;
    }
}