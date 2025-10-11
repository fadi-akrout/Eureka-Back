package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PanelisteRepository extends JpaRepository<Paneliste, Integer>, JpaSpecificationExecutor<Paneliste> {
    Optional<Paneliste> findByEmail(String email);
    
    @Query("SELECT p FROM Paneliste p LEFT JOIN FETCH p.campagnesParticipees cp WHERE p.id = :id AND (cp.statut IN ('ACCEPTE', 'EN_COURS', 'COMPLETE') OR cp.statut IS NULL)")
    Optional<Paneliste> findByIdWithCampaigns(@Param("id") Integer id);
    
    @Query("SELECT COUNT(cp) FROM PanelisteCampagne cp WHERE cp.paneliste.id = :panelisteId AND cp.statut IN ('ACCEPTE', 'EN_COURS', 'COMPLETE')")
    Long countParticipatedCampaigns(@Param("panelisteId") Integer panelisteId);
    
    /**
     * Find panelists available for invitation to a specific campaign
     * This excludes panelists who already have a relationship with the campaign
     */
    @Query("SELECT p FROM Paneliste p WHERE p.id NOT IN " +
           "(SELECT pc.paneliste.id FROM PanelisteCampagne pc WHERE pc.campagne.id = :campaignId)")
    Page<Paneliste> findAvailablePanelistsForCampaign(
            @Param("campaignId") Integer campaignId, 
            Pageable pageable);
    
    /**
     * Find panelists available for invitation with search capability
     */
    @Query("SELECT p FROM Paneliste p WHERE p.id NOT IN " +
           "(SELECT pc.paneliste.id FROM PanelisteCampagne pc WHERE pc.campagne.id = :campaignId) " +
           "AND (LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Paneliste> findAvailablePanelistsForCampaignWithSearch(
            @Param("campaignId") Integer campaignId,
            @Param("search") String search,
            Pageable pageable);
}
