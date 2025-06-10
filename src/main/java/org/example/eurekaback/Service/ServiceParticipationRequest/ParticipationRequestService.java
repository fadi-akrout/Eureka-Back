package org.example.eurekaback.Service.ServiceParticipationRequest;

import org.example.eurekaback.Dto.ParticipationRequestDTO;
import org.example.eurekaback.Dto.ParticipationStatusDTO;
import org.example.eurekaback.Entity.PanelisteCampagne;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestService {
    
    /**
     * Submit a participation request
     */
    PanelisteCampagne requestParticipation(Integer campagneId, Integer panelisteId);
    
    /**
     * Get pending participation requests for a campaign
     */
    List<ParticipationRequestDTO> getPendingRequests(Integer campagneId);
    
    /**
     * Approve a participation request
     */
    PanelisteCampagne approveRequest(Integer campagneId, Integer panelisteId, Integer annonceurId);
    
    /**
     * Reject a participation request
     */
    PanelisteCampagne rejectRequest(Integer campagneId, Integer panelisteId, Integer annonceurId, String feedback);
    
    /**
     * Get participation status for a panelist in a campaign
     */
    Optional<ParticipationStatusDTO> getParticipationStatus(Integer campagneId, Integer panelisteId);
    
    /**
     * Cancel a pending participation request
     */
    void cancelRequest(Integer campagneId, Integer panelisteId);
    
    /**
     * Get all participation requests for a panelist
     */
    List<ParticipationRequestDTO> getRequestsByPanelist(Integer panelisteId);
    
    /**
     * Count pending requests for a campaign
     */
    long countPendingRequests(Integer campagneId);
}