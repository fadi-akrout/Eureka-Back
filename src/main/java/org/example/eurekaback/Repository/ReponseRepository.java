package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Reponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReponseRepository extends JpaRepository<Reponse, Integer> {
    
    /**
     * Find all responses for a specific formulaire
     */
    List<Reponse> findByFormulaireId(Integer formulaireId);
    
    /**
     * Find all responses submitted by a specific paneliste
     */
    List<Reponse> findByPanelisteId(Integer panelisteId);
    
    /**
     * Find all responses for a specific question
     */
    List<Reponse> findByQuestionId(Integer questionId);
    
    /**
     * Find all responses for a specific paneliste and formulaire
     */
    List<Reponse> findByPanelisteIdAndFormulaireId(Integer panelisteId, Integer formulaireId);
    
    /**
     * Find all responses for a specific campaign and paneliste
     */
    @Query("SELECT r FROM Reponse r " +
           "JOIN r.question q " +
           "JOIN FormulaireQuestion fq ON fq.question.id = q.id " +
           "JOIN fq.formulaire f " +
           "WHERE f.campagne.id = :campaignId AND r.panelisteId = :panelisteId")
    List<Reponse> findByCampaignIdAndPanelisteId(@Param("campaignId") Integer campaignId, @Param("panelisteId") Integer panelisteId);
    
    /**
     * Find all responses for a specific campaign
     */
    @Query("SELECT r FROM Reponse r " +
           "JOIN r.question q " +
           "JOIN FormulaireQuestion fq ON fq.question.id = q.id " +
           "JOIN fq.formulaire f " +
           "WHERE f.campagne.id = :campaignId")
    List<Reponse> findByCampaignId(@Param("campaignId") Integer campaignId);
}
