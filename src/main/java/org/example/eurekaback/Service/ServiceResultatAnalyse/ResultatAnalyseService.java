package org.example.eurekaback.Service.ServiceResultatAnalyse;

import org.example.eurekaback.Dto.SentimentAnalyticsDTO;
import org.example.eurekaback.Entity.ResultatAnalyse;

import java.util.List;
import java.util.Optional;

public interface ResultatAnalyseService {
    List<ResultatAnalyse> getAllResultatsAnalyse();
    Optional<ResultatAnalyse> getResultatAnalyseById(int id);
    ResultatAnalyse createResultatAnalyse(ResultatAnalyse resultatAnalyse);
    void deleteResultatAnalyse(int id);
    ResultatAnalyse genererAnalysePourCampagne(int campagneId);

    /**
     * Récupère l'analyse de sentiments pour une campagne donnée
     */
    SentimentAnalyticsDTO getSentimentAnalyticsByCampaignId(Integer campaignId);
}
