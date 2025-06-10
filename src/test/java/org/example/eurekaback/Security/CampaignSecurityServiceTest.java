package org.example.eurekaback.Security;

import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.CampagneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CampaignSecurityServiceTest {

    private CampaignSecurityService campaignSecurityService;

    @Mock
    private CampagneRepository campagneRepository;

    @Mock
    private AnnonceurRepository annonceurRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        campaignSecurityService = new CampaignSecurityService(campagneRepository, annonceurRepository);
    }

    @Test
    void isOwner_whenUserIsOwner_returnsTrue() {
        // Setup
        Integer campagneId = 1;
        String email = "owner@example.com";
        
        Annonceur annonceur = new Annonceur();
        annonceur.setId(5);
        annonceur.setEmail(email);
        
        Campagne campagne = new Campagne();
        campagne.setId(campagneId);
        campagne.setAnnonceur(annonceur);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, 
                "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANNONCEUR"))
        );
        
        when(annonceurRepository.findByEmail(email)).thenReturn(Optional.of(annonceur));
        when(campagneRepository.findById(campagneId)).thenReturn(Optional.of(campagne));
        
        // Execute
        boolean result = campaignSecurityService.isOwner(authentication, campagneId);
        
        // Verify
        assertTrue(result);
        verify(annonceurRepository).findByEmail(email);
        verify(campagneRepository).findById(campagneId);
    }

    @Test
    void isOwner_whenUserIsNotOwner_returnsFalse() {
        // Setup
        Integer campagneId = 1;
        String email = "not-owner@example.com";
        
        Annonceur annonceur = new Annonceur();
        annonceur.setId(5);
        annonceur.setEmail(email);
        
        Annonceur otherAnnonceur = new Annonceur();
        otherAnnonceur.setId(10);
        
        Campagne campagne = new Campagne();
        campagne.setId(campagneId);
        campagne.setAnnonceur(otherAnnonceur);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, 
                "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANNONCEUR"))
        );
        
        when(annonceurRepository.findByEmail(email)).thenReturn(Optional.of(annonceur));
        when(campagneRepository.findById(campagneId)).thenReturn(Optional.of(campagne));
        
        // Execute
        boolean result = campaignSecurityService.isOwner(authentication, campagneId);
        
        // Verify
        assertFalse(result);
        verify(annonceurRepository).findByEmail(email);
        verify(campagneRepository).findById(campagneId);
    }

    @Test
    void isOwner_whenCampagneNotFound_returnsFalse() {
        // Setup
        Integer campagneId = 999;
        String email = "owner@example.com";
        
        Annonceur annonceur = new Annonceur();
        annonceur.setId(5);
        annonceur.setEmail(email);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, 
                "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANNONCEUR"))
        );
        
        when(annonceurRepository.findByEmail(email)).thenReturn(Optional.of(annonceur));
        when(campagneRepository.findById(campagneId)).thenReturn(Optional.empty());
        
        // Execute
        boolean result = campaignSecurityService.isOwner(authentication, campagneId);
        
        // Verify
        assertFalse(result);
        verify(annonceurRepository).findByEmail(email);
        verify(campagneRepository).findById(campagneId);
    }

    @Test
    void isOwner_whenUserNotFound_returnsFalse() {
        // Setup
        Integer campagneId = 1;
        String email = "nonexistent@example.com";
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, 
                "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANNONCEUR"))
        );
        
        when(annonceurRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Execute
        boolean result = campaignSecurityService.isOwner(authentication, campagneId);
        
        // Verify
        assertFalse(result);
        verify(annonceurRepository).findByEmail(email);
        verify(campagneRepository, never()).findById(any());
    }
}