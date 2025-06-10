package org.example.eurekaback.Service;

import org.example.eurekaback.Dto.UserDTO;
import org.example.eurekaback.Dto.UserStatsDTO;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.AdminRepository;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserStatsService {
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private AnnonceurRepository annonceurRepository;
    
    @Autowired
    private PanelisteRepository panelisteRepository;
    
    public UserStatsDTO getUserStats() {
        UserStatsDTO stats = new UserStatsDTO();
        
        // Count totals
        stats.setTotalUsers(utilisateurRepository.count());
        stats.setTotalAdmins(adminRepository.count());
        stats.setTotalAnnonceurs(annonceurRepository.count());
        stats.setTotalPanelistes(panelisteRepository.count());
        
        // Get recent registrations (last 30 days, limit 10)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = calendar.getTime();
        
        List<Utilisateur> recentUsers = utilisateurRepository
            .findByDateInscriptionAfterOrderByDateInscriptionDesc(thirtyDaysAgo)
            .stream()
            .limit(10)
            .collect(Collectors.toList());
            
        stats.setRecentRegistrations(recentUsers.stream()
            .map(this::convertToUserDTO)
            .collect(Collectors.toList()));
            
        return stats;
    }
    
    private UserDTO convertToUserDTO(Utilisateur utilisateur) {
        UserDTO dto = new UserDTO();
        dto.setId((long) utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setEmail(utilisateur.getEmail());
        dto.setUserType(utilisateur.getRole());
        dto.setDateInscription(utilisateur.getDateInscription());
        return dto;
    }
}