package org.example.eurekaback.Service.ServicePanelisteCampagne;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Entity.PanelisteCampagne.StatutParticipation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PanelisteCampagneService {
    List<PanelisteCampagne> getAllPanelisteCampagnes();
    Optional<PanelisteCampagne> getPanelisteCampagneById(int id);
    PanelisteCampagne createPanelisteCampagne(PanelisteCampagne panelisteCampagne);
    void deletePanelisteCampagne(int id);
    
    // New methods for Panelist Invitation System
    
    /**
     * Finds panelists available for invitation to a specific campaign
     * @param campaignId the campaign ID
     * @param search optional search term for filtering panelists
     * @param pageable pagination information
     * @return Page of available panelists
     */
    Page<Paneliste> getAvailablePanelistesForCampaign(int campaignId, String search, Pageable pageable);
    
    /**
     * Invites one or more panelists to a campaign
     * @param campaignId the campaign ID
     * @param panelisteIds list of panelist IDs to invite
     * @return map containing success/failure information
     */
    Map<String, Object> invitePanelistesToCampaign(int campaignId, List<Integer> panelisteIds);
    
    /**
     * Gets all invitations for a specific panelist
     * @param panelisteId the panelist ID
     * @return list of campaign invitations
     */
    List<PanelisteCampagne> getInvitationsForPaneliste(int panelisteId);
    
    /**
     * Accepts a campaign invitation
     * @param panelisteId the panelist ID
     * @param campaignId the campaign ID
     * @return the updated PanelisteCampagne entity
     */
    PanelisteCampagne acceptInvitation(int panelisteId, int campaignId);
    
    /**
     * Rejects a campaign invitation
     * @param panelisteId the panelist ID
     * @param campaignId the campaign ID
     * @return the updated PanelisteCampagne entity
     */
    PanelisteCampagne rejectInvitation(int panelisteId, int campaignId);
    
    /**
     * Gets the current participation status of a panelist in a campaign
     * @param panelisteId the panelist ID
     * @param campaignId the campaign ID
     * @return the PanelisteCampagne entity representing the relationship
     */
    Optional<PanelisteCampagne> getParticipationStatus(int panelisteId, int campaignId);
    
    /**
     * Updates the status of a panelist in a campaign
     * @param panelisteId the panelist ID
     * @param campaignId the campaign ID
     * @param status the new status
     * @return the updated PanelisteCampagne entity
     */
    PanelisteCampagne updateParticipationStatus(int panelisteId, int campaignId, StatutParticipation status);
}
