package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity

public class Paneliste extends Utilisateur {

    @ElementCollection
    private List<String> interets = new ArrayList<>();

    @OneToMany(mappedBy = "paneliste", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    private List<PanelisteCampagne> campagnesParticipees = new ArrayList<>();

    // Helper method to invite to a Campagne
    public void inviterCampagne(Campagne campagne) {
        for (PanelisteCampagne pc : campagnesParticipees) {
            if (pc.getCampagne().equals(campagne)) {
                return; // Évite les doublons
            }
        }

        PanelisteCampagne panelisteCampagne = new PanelisteCampagne();
        panelisteCampagne.setPaneliste(this);
        panelisteCampagne.setCampagne(campagne);
        panelisteCampagne.setStatut(PanelisteCampagne.StatutParticipation.INVITE);
        panelisteCampagne.setDateInvitation(java.time.LocalDateTime.now());

        campagnesParticipees.add(panelisteCampagne);
        campagne.addPanelisteCampagne(panelisteCampagne);
    }

    // Helper method to accept campaign participation
    public void accepterCampagne(Campagne campagne) {
        for (PanelisteCampagne pc : campagnesParticipees) {
            if (pc.getCampagne().equals(campagne)) {
                pc.setStatut(PanelisteCampagne.StatutParticipation.ACCEPTE);
                pc.setDateReponse(java.time.LocalDateTime.now());
                return;
            }
        }
    }

    // Helper method to refuse campaign participation
    public void refuserCampagne(Campagne campagne) {
        for (PanelisteCampagne pc : campagnesParticipees) {
            if (pc.getCampagne().equals(campagne)) {
                pc.setStatut(PanelisteCampagne.StatutParticipation.REFUSE);
                pc.setDateReponse(java.time.LocalDateTime.now());
                return;
            }
        }
    }

    // Helper method to participate in a campaign
    public void participerCampagne(Campagne campagne) {
        // Check if already participating
        for (PanelisteCampagne pc : campagnesParticipees) {
            if (pc.getCampagne().equals(campagne)) {
                // If already exists, update status to ACCEPTE if not already
                if (pc.getStatut() == PanelisteCampagne.StatutParticipation.INVITE) {
                    pc.setStatut(PanelisteCampagne.StatutParticipation.ACCEPTE);
                    pc.setDateReponse(java.time.LocalDateTime.now());
                }
                return;
            }
        }

        // If not exists, create new participation
        PanelisteCampagne panelisteCampagne = new PanelisteCampagne();
        panelisteCampagne.setPaneliste(this);
        panelisteCampagne.setCampagne(campagne);
        panelisteCampagne.setStatut(PanelisteCampagne.StatutParticipation.ACCEPTE);
        panelisteCampagne.setDateInvitation(java.time.LocalDateTime.now());
        panelisteCampagne.setDateReponse(java.time.LocalDateTime.now());

        campagnesParticipees.add(panelisteCampagne);
        campagne.addPanelisteCampagne(panelisteCampagne);
    }

    // Helper method to request participation in a campaign
    public void demanderParticipationCampagne(Campagne campagne) {
        // Check if already has a relationship with this campaign
        for (PanelisteCampagne pc : campagnesParticipees) {
            if (pc.getCampagne().equals(campagne)) {
                // If already exists and was refused, allow to request again
                if (pc.getStatut() == PanelisteCampagne.StatutParticipation.REFUSE) {
                    pc.setStatut(PanelisteCampagne.StatutParticipation.PENDING);
                    pc.setDateRequest(java.time.LocalDateTime.now());
                    pc.setDateApproval(null);
                    pc.setFeedback(null);
                }
                return;
            }
        }

        // If not exists, create new participation request
        PanelisteCampagne panelisteCampagne = new PanelisteCampagne();
        panelisteCampagne.setPaneliste(this);
        panelisteCampagne.setCampagne(campagne);
        panelisteCampagne.setStatut(PanelisteCampagne.StatutParticipation.PENDING);
        panelisteCampagne.setDateRequest(java.time.LocalDateTime.now());

        campagnesParticipees.add(panelisteCampagne);
        campagne.addPanelisteCampagne(panelisteCampagne);
    }
}
