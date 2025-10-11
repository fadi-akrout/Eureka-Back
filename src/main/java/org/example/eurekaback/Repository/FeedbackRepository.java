package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    @Query("SELECT f FROM Feedback f WHERE f.campagne.id = :campaignId AND f.paneliste.id = :panelisteId")
    Optional<Feedback> findByCampagneIdAndPanelisteId(@Param("campaignId") Long campaignId, @Param("panelisteId") Long panelisteId);

    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.campagne.id = :campaignId AND f.paneliste.id = :panelisteId")
    boolean existsByCampagneIdAndPanelisteId(@Param("campaignId") Long campaignId, @Param("panelisteId") Long panelisteId);

    @Query("SELECT f FROM Feedback f WHERE f.paneliste.id = :panelisteId ORDER BY f.date DESC")
    List<Feedback> findByPanelisteIdOrderByDateDesc(@Param("panelisteId") Long panelisteId);

    @Query("SELECT AVG(f.note) FROM Feedback f WHERE f.campagne.id = :campaignId")
    Double getAverageRatingByCampaignId(@Param("campaignId") Long campaignId);

    @Query("SELECT f.note, COUNT(f) FROM Feedback f WHERE f.campagne.id = :campaignId GROUP BY f.note")
    List<Object[]> getRatingDistributionByCampaignId(@Param("campaignId") Long campaignId);
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.campagne.id = :campaignId")
    Long countByCampaignId(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.paneliste.id = :panelisteId AND LENGTH(TRIM(f.commentaire)) > 20")
    Long countDetailedFeedbacksByPanelisteId(@Param("panelisteId") Long panelisteId);
}
