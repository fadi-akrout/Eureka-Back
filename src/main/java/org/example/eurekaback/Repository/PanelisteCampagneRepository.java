package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PanelisteCampagneRepository extends JpaRepository<PanelisteCampagne, Integer> {
    List<PanelisteCampagne> findByPaneliste(Paneliste paneliste);
    List<PanelisteCampagne> findByCampagne(Campagne campagne);
    
    // Find only participated campaigns (excluding invitations and refusals)
    @Query("SELECT pc FROM PanelisteCampagne pc JOIN FETCH pc.campagne WHERE pc.paneliste = :paneliste AND pc.statut IN ('ACCEPTE', 'EN_COURS', 'COMPLETE')")
    List<PanelisteCampagne> findParticipatedCampaignsByPaneliste(@Param("paneliste") Paneliste paneliste);
    
    // Find campaigns by status
    List<PanelisteCampagne> findByPanelisteAndStatut(Paneliste paneliste, PanelisteCampagne.StatutParticipation statut);
    
    // Count participated campaigns
    @Query("SELECT COUNT(pc) FROM PanelisteCampagne pc WHERE pc.paneliste.id = :panelisteId AND pc.statut IN ('ACCEPTE', 'EN_COURS', 'COMPLETE')")
    Long countParticipatedCampaignsByPanelisteId(@Param("panelisteId") Integer panelisteId);
    
    // Simple count method for migration service
    Long countByPanelisteId(Integer panelisteId);

    // New methods for participation request system
    
    // Find participation record by campaign and panelist
    Optional<PanelisteCampagne> findByCampagneIdAndPanelisteId(Integer campagneId, Integer panelisteId);
    
    // Find all pending requests for a campaign
    List<PanelisteCampagne> findByCampagneIdAndStatut(Integer campagneId, PanelisteCampagne.StatutParticipation statut);
    
    // Find all pending requests for a campaign with panelist details
    @Query("SELECT pc FROM PanelisteCampagne pc " +
           "JOIN FETCH pc.paneliste p " +
           "WHERE pc.campagne.id = :campagneId AND pc.statut = :statut " +
           "ORDER BY pc.dateRequest ASC")
    List<PanelisteCampagne> findPendingRequestsWithPanelistDetails(
        @Param("campagneId") Integer campagneId, 
        @Param("statut") PanelisteCampagne.StatutParticipation statut
    );
    
    // Check if panelist has any relationship with campaign
    boolean existsByCampagneIdAndPanelisteId(Integer campagneId, Integer panelisteId);
    
    // Find all campaigns for a panelist with specific status
    List<PanelisteCampagne> findByPanelisteIdAndStatut(Integer panelisteId, PanelisteCampagne.StatutParticipation statut);
    
    // Count pending requests for a campaign
    long countByCampagneIdAndStatut(Integer campagneId, PanelisteCampagne.StatutParticipation statut);
}
