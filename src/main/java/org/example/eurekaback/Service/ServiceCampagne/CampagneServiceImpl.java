package org.example.eurekaback.Service.ServiceCampagne;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.*;
import org.example.eurekaback.Repository.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CampagneServiceImpl implements CampagneService {
    private final CampagneRepository campagneRepository;
    private final ProduitRepository produitRepository;
    private final FormulaireRepository formulaireRepository;
    private final PanelisteCampagneRepository panelisteCampagneRepository;
    private final ResultatAnalyseRepository resultatAnalyseRepository;

    @Override
    public List<Campagne> getAllCampagnes() {
        return campagneRepository.findAll();
    }

    @Override
    public Optional<Campagne> getCampagneById(int id) {
        return campagneRepository.findById(id);
    }

    @Override
    public Campagne createCampagne(Campagne campagne) {
        return campagneRepository.save(campagne);
    }

    @Override
    public void deleteCampagne(int id) {
        campagneRepository.deleteById(id);
    }
    @Override
    @Transactional
    public Campagne createCampagneWithProduitAndFormulaire(Campagne campagne, Produit produit, Formulaire formulaire) {
        // Save campagne first
        Campagne savedCampagne = campagneRepository.save(campagne);

        // Set bidirectional relationship for Produit
        produit.setCampagne(savedCampagne);
        Produit savedProduit = produitRepository.save(produit);

        // Important: Set the product back on the campaign
        savedCampagne.setProduit(savedProduit);

        // Set bidirectional relationship for Formulaire
        formulaire.setCampagne(savedCampagne);
        Formulaire savedFormulaire = formulaireRepository.save(formulaire);

        // Important: Set the form back on the campaign
        savedCampagne.setFormulaire(savedFormulaire);

        // Save the updated campaign
        return campagneRepository.save(savedCampagne);
    }
    @Override
    public List<Campagne> getCampagnesActives() {
        // Get current date
        Date currentDate = new Date();

        // Find all campaigns and filter active ones
        return campagneRepository.findAll().stream()
                .filter(campagne -> "ACTIVE".equals(campagne.getStatut()) ||
                        ("PLANIFIEE".equals(campagne.getStatut()) &&
                                campagne.getDateDebut() != null &&
                                campagne.getDateDebut().before(currentDate) &&
                                (campagne.getDateFin() == null || campagne.getDateFin().after(currentDate))))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignerPanelistesACampagne(Campagne campagne, List<Paneliste> panelistes) {
        for (Paneliste paneliste : panelistes) {
            // Check if the association already exists
            boolean alreadyExists = panelisteCampagneRepository.findByCampagne(campagne).stream()
                    .anyMatch(pc -> pc.getPaneliste().getId() == paneliste.getId());

            if (!alreadyExists) {
                PanelisteCampagne panelisteCampagne = new PanelisteCampagne();
                panelisteCampagne.setCampagne(campagne);
                panelisteCampagne.setPaneliste(paneliste);
                panelisteCampagneRepository.save(panelisteCampagne);
            }
        }
    }

    @Override
    @Transactional
    public void demarrerCampagne(int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        campagne.setStatut("ACTIVE");
        campagne.setDateDebut(new Date());
        campagneRepository.save(campagne);
    }

    @Override
    @Transactional
    public void terminerCampagne(int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + campagneId));

        campagne.setStatut("TERMINEE");
        campagne.setDateFin(new Date());
        campagneRepository.save(campagne);

        // Note: We're not automatically generating analysis results here
        // The user will need to explicitly call the analysis endpoint
    }
}