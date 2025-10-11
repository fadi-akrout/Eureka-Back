package org.example.eurekaback.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.PanelisteCampagne;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PanelisteCampagneWithCampaignDTO {
    private int id;
    private int panelisteId;
    private int campagneId;
    private PanelisteCampagne.StatutParticipation statut;
    private LocalDateTime dateInvitation;
    private LocalDateTime dateReponse;
    private LocalDateTime dateCompletion;
    private LocalDateTime dateRequest;
    private LocalDateTime dateApproval;
    private String feedback;
    private Campagne campagne;

    // Constructor to create DTO from PanelisteCampagne entity
    public PanelisteCampagneWithCampaignDTO(PanelisteCampagne panelisteCampagne) {
        this.id = panelisteCampagne.getId();
        this.panelisteId = panelisteCampagne.getPaneliste() != null ? panelisteCampagne.getPaneliste().getId() : 0;
        this.campagneId = panelisteCampagne.getCampagne() != null ? panelisteCampagne.getCampagne().getId() : 0;
        this.statut = panelisteCampagne.getStatut();
        this.dateInvitation = panelisteCampagne.getDateInvitation();
        this.dateReponse = panelisteCampagne.getDateReponse();
        this.dateCompletion = panelisteCampagne.getDateCompletion();
        this.dateRequest = panelisteCampagne.getDateRequest();
        this.dateApproval = panelisteCampagne.getDateApproval();
        this.feedback = panelisteCampagne.getFeedback();
        this.campagne = panelisteCampagne.getCampagne();
    }
}