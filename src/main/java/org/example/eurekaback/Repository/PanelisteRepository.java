package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PanelisteRepository extends JpaRepository<Paneliste, Integer> {
    Optional<Paneliste> findByEmail(String email);
    
    @Query("SELECT p FROM Paneliste p LEFT JOIN FETCH p.campagnesParticipees cp WHERE p.id = :id AND (cp.statut IN ('ACCEPTE', 'EN_COURS', 'COMPLETE') OR cp.statut IS NULL)")
    Optional<Paneliste> findByIdWithCampaigns(@Param("id") Integer id);
    
    @Query("SELECT COUNT(cp) FROM PanelisteCampagne cp WHERE cp.paneliste.id = :panelisteId AND cp.statut IN ('ACCEPTE', 'EN_COURS', 'COMPLETE')")
    Long countParticipatedCampaigns(@Param("panelisteId") Integer panelisteId);
}
