package org.example.eurekaback.Service.ServicePaneliste;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PanelisteServiceImpl implements PanelisteService {
    
    private static final Logger logger = LoggerFactory.getLogger(PanelisteServiceImpl.class);
    
    private final PanelisteRepository panelisteRepository;
    private final CampagneRepository campagneRepository;
    private final PanelisteCampagneRepository panelisteCampagneRepository;

    @Override
    public List<Paneliste> getAllPanelistes() {
        return panelisteRepository.findAll();
    }

    @Override
    public Optional<Paneliste> getPanelisteById(int id) {
        logger.info("Finding paneliste with ID: {}", id);
        
        Optional<Paneliste> panelisteOpt = panelisteRepository.findById(id);
        
        if (panelisteOpt.isEmpty()) {
            logger.warn("Paneliste with ID {} not found", id);
            return panelisteOpt;
        }
        
        Paneliste paneliste = panelisteOpt.get();
        logger.info("Found paneliste: {} with email: {}", paneliste.getNom(), paneliste.getEmail());
        logger.info("Paneliste has {} interests", paneliste.getInterets() != null ? paneliste.getInterets().size() : 0);
        
        return panelisteOpt;
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
        logger.info("Getting campaigns participated by paneliste ID: {}", panelisteId);
        
        Paneliste paneliste = panelisteRepository.findById(panelisteId)
                .orElseThrow(() -> new RuntimeException("Paneliste not found with id: " + panelisteId));

        List<PanelisteCampagne> participations = panelisteCampagneRepository.findByPaneliste(paneliste);
        logger.info("Found {} participations for paneliste {}", participations.size(), panelisteId);
        
        if (participations.isEmpty()) {
            logger.warn("No participations found for paneliste {}", panelisteId);
            return new java.util.ArrayList<>();
        }

        List<Campagne> campagnes = participations.stream()
                .peek(pc -> logger.debug("Processing participation: ID={}, Status={}, Campaign={}", 
                            pc.getId(), pc.getStatut(), pc.getCampagne() != null ? pc.getCampagne().getNom() : "NULL"))
                .map(PanelisteCampagne::getCampagne)
                .filter(campagne -> {
                    if (campagne == null) {
                        logger.error("Found null campaign in participation");
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
                
        logger.info("Returning {} campaigns for paneliste {}", campagnes.size(), panelisteId);
        return campagnes;
    }

    @Override
    @Transactional
    public void participerACampagne(int panelisteId, int campagneId) {
        Paneliste paneliste = panelisteRepository.findById(panelisteId)
                .orElseThrow(() -> new RuntimeException("Paneliste not found with id: " + panelisteId));

        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        // The participerCampagne method handles duplicate checking internally
        paneliste.participerCampagne(campagne);
        panelisteRepository.save(paneliste);
    }
    
    @Override
    public Paneliste updatePaneliste(int id, Paneliste updatedPaneliste) {
        Paneliste existingPaneliste = panelisteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paneliste not found with id: " + id));
        
        // Update basic information
        if (updatedPaneliste.getNom() != null) {
            existingPaneliste.setNom(updatedPaneliste.getNom());
        }

        
        return panelisteRepository.save(existingPaneliste);
    }
}