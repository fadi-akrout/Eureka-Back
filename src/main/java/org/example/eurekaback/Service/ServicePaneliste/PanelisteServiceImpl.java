package org.example.eurekaback.Service.ServicePaneliste;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PanelisteServiceImpl implements PanelisteService {
    private final PanelisteRepository panelisteRepository;
    private final CampagneRepository campagneRepository;
    private final PanelisteCampagneRepository panelisteCampagneRepository;

    @Override
    public List<Paneliste> getAllPanelistes() {
        return panelisteRepository.findAll();
    }

    @Override
    public Optional<Paneliste> getPanelisteById(int id) {
        return panelisteRepository.findById(id);
    }

    @Override
    public Paneliste createPaneliste(Paneliste paneliste) {
        return panelisteRepository.save(paneliste);
    }

    @Override
    public void deletePaneliste(int id) {
        panelisteRepository.deleteById(id);
    }
    @Override
    public List<Campagne> getCampagnesParticipees(int panelisteId) {
        Paneliste paneliste = panelisteRepository.findById(panelisteId)
                .orElseThrow(() -> new RuntimeException("Paneliste not found with id: " + panelisteId));

        List<PanelisteCampagne> participations = panelisteCampagneRepository.findByPaneliste(paneliste);

        return participations.stream()
                .map(PanelisteCampagne::getCampagne)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void participerACampagne(int panelisteId, int campagneId) {
        Paneliste paneliste = panelisteRepository.findById(panelisteId)
                .orElseThrow(() -> new RuntimeException("Paneliste not found with id: " + panelisteId));

        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        // Check if already participating
        boolean alreadyParticipating = panelisteCampagneRepository.findByPaneliste(paneliste).stream()
                .anyMatch(pc -> pc.getCampagne().getId() == campagneId);

        if (!alreadyParticipating) {
            paneliste.participerCampagne(campagne);
            panelisteRepository.save(paneliste);
        }
    }
}