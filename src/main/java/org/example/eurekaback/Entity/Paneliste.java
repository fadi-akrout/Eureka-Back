package org.example.eurekaback.Entity;
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

    @OneToMany(mappedBy = "paneliste", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PanelisteCampagne> campagnesParticipees = new ArrayList<>();

    // Helper method to participate in a Campagne
    public void participerCampagne(Campagne campagne) {
        for (PanelisteCampagne pc : campagnesParticipees) {
            if (pc.getCampagne().equals(campagne)) {
                return; // Évite les doublons
            }
        }

        PanelisteCampagne panelisteCampagne = new PanelisteCampagne();
        panelisteCampagne.setPaneliste(this);
        panelisteCampagne.setCampagne(campagne);

        campagnesParticipees.add(panelisteCampagne);
        campagne.addPanelisteCampagne(panelisteCampagne);
    }
}
