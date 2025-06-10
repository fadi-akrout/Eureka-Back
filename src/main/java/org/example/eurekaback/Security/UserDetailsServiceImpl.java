package org.example.eurekaback.Security;

import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private AnnonceurRepository annonceurRepository;
    
    @Autowired
    private PanelisteRepository panelisteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First try to find the user in the Annonceur repository
        Optional<? extends Utilisateur> utilisateur = annonceurRepository.findByEmail(email);
        
        // If not found, try the Paneliste repository
        if (utilisateur.isEmpty()) {
            utilisateur = panelisteRepository.findByEmail(email);
        }
        
        // If still not found, try the general Utilisateur repository (for backward compatibility)
        if (utilisateur.isEmpty()) {
            utilisateur = utilisateurRepository.findByEmail(email);
        }
        
        // If not found in any repository, throw an exception
        if (utilisateur.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        return utilisateur.get();
    }
}