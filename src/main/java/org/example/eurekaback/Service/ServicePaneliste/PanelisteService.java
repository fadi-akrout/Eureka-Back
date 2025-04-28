package org.example.eurekaback.Service.ServicePaneliste;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;

import java.util.List;
import java.util.Optional;

public interface PanelisteService {
    List<Paneliste> getAllPanelistes();
    Optional<Paneliste> getPanelisteById(int id);
    Paneliste createPaneliste(Paneliste paneliste);
    void deletePaneliste(int id);

    List<Campagne> getCampagnesParticipees(int panelisteId);
    void participerACampagne(int panelisteId, int campagneId);
}
