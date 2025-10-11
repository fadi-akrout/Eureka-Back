package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.ResultatAnalyse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResultatAnalyseRepository extends JpaRepository<ResultatAnalyse, Integer> {
    
    /**
     * Find ResultatAnalyse by campaign ID
     */
    @Query("SELECT ra FROM ResultatAnalyse ra WHERE ra.campagne.id = :campaignId")
    Optional<ResultatAnalyse> findByCampaignId(@Param("campaignId") Integer campaignId);
    
    /**
     * Check if sentiment analysis exists for a campaign
     */
    @Query("SELECT CASE WHEN COUNT(ra) > 0 THEN true ELSE false END " +
           "FROM ResultatAnalyse ra WHERE ra.campagne.id = :campaignId " +
           "AND ra.sentimentAnalysisData IS NOT NULL")
    boolean existsSentimentAnalysisForCampaign(@Param("campaignId") Integer campaignId);
    
    /**
     * Find all ResultatAnalyse with sentiment analysis data
     */
    @Query("SELECT ra FROM ResultatAnalyse ra WHERE ra.sentimentAnalysisData IS NOT NULL")
    java.util.List<ResultatAnalyse> findAllWithSentimentAnalysis();
    
    /**
     * Count campaigns with sentiment analysis
     */
    @Query("SELECT COUNT(ra) FROM ResultatAnalyse ra WHERE ra.sentimentAnalysisData IS NOT NULL")
    long countCampaignsWithSentimentAnalysis();
}
