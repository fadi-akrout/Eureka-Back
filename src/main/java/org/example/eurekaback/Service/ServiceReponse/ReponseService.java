package org.example.eurekaback.Service.ServiceReponse;

import org.example.eurekaback.DTO.ReponseDTO;
import org.example.eurekaback.DTO.ReponseDetailDTO;
import org.example.eurekaback.Entity.Reponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface ReponseService {
    List<Reponse> getAllReponses();
    Optional<Reponse> getReponseById(Integer id);
    Reponse createReponse(Reponse reponse);
    void deleteReponse(Integer id);
    
    /**
     * Save multiple responses in a batch operation
     * @param responseDTOs List of response DTOs to save
     * @param campaignId ID of the campaign these responses belong to
     * @param panelisteId ID of the panelist submitting the responses
     */
    void saveBatchResponses(List<ReponseDTO> responseDTOs, Integer campaignId, Integer panelisteId);
    
    /**
     * Get all responses for a specific formulaire
     */
    List<Reponse> getResponsesByFormulaireId(Integer formulaireId);
    
    /**
     * Get all responses submitted by a specific paneliste
     */
    List<Reponse> getResponsesByPanelisteId(Integer panelisteId);
    
    /**
     * Get all responses for a specific paneliste and formulaire
     */
    List<Reponse> getResponsesByPanelisteIdAndFormulaireId(Integer panelisteId, Integer formulaireId);
    
    /**
     * Get all responses for a specific campaign and paneliste
     * Used to check if a panelist has already submitted responses for a campaign
     */
    List<Reponse> getResponsesByCampaignIdAndPanelisteId(Integer campaignId, Integer panelisteId);
    
    /**
     * Get all responses for a specific campaign with detailed information including panelist data
     * @param campaignId ID of the campaign
     * @return List of detailed response DTOs
     */
    List<ReponseDetailDTO> getResponsesByCampaignWithDetails(Integer campaignId);
    
    /**
     * Get all responses for a specific campaign and panelist with detailed information
     * @param campaignId ID of the campaign
     * @param panelisteId ID of the panelist
     * @return List of detailed response DTOs
     */
    List<ReponseDetailDTO> getResponsesByCampaignAndPanelisteWithDetails(Integer campaignId, Integer panelisteId);
    
    /**
     * Export all responses for a specific campaign to CSV
     * @param campaignId ID of the campaign
     * @return Resource containing the CSV file
     */
    ResponseEntity<Resource> exportResponsesToCsv(Integer campaignId);
}
