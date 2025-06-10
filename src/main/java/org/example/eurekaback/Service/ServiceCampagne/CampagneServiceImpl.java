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
    private final AnnonceurRepository annonceurRepository;

    @Override
    public List<Campagne> getAllCampagnes() {
        List<Campagne> campagnes = campagneRepository.findAll();
        
        // Ensure annonceurId is set for all campaigns
        for (Campagne campagne : campagnes) {
            if (campagne.getAnnonceur() != null) {
                campagne.setAnnonceurId(campagne.getAnnonceur().getId());
            }
        }
        
        return campagnes;
    }

    @Override
    public Optional<Campagne> getCampagneById(int id) {
        Optional<Campagne> campagneOpt = campagneRepository.findById(id);
        
        // Ensure annonceurId is set if the campaign is found
        campagneOpt.ifPresent(campagne -> {
            if (campagne.getAnnonceur() != null) {
                campagne.setAnnonceurId(campagne.getAnnonceur().getId());
            }
        });
        
        return campagneOpt;
    }

    @Override
    public Campagne createCampagne(Campagne campagne) {
        // Handle the case where annonceurId is set but annonceur object is not
        Integer annonceurId = campagne.getAnnonceurId();
        if (annonceurId != null && campagne.getAnnonceur() == null) {
            annonceurRepository.findById(annonceurId)
                .ifPresent(campagne::setAnnonceur);
        }
        
        // If annonceur is set, make sure it's properly linked
        if (campagne.getAnnonceur() != null) {
            // Make sure the annonceur exists in the database
            Annonceur annonceur = annonceurRepository.findById(campagne.getAnnonceur().getId())
                .orElse(campagne.getAnnonceur());
            
            // Set the annonceur and update the annonceurId
            campagne.setAnnonceur(annonceur);
            campagne.setAnnonceurId(annonceur.getId());
            
            // Add the campaign to the annonceur's list of campaigns
            if (!annonceur.getCampagnesCreees().contains(campagne)) {
                annonceur.getCampagnesCreees().add(campagne);
            }
        }
        
        // Save the campaign
        Campagne savedCampagne = campagneRepository.save(campagne);
        
        // Make sure annonceurId is set for the response
        if (savedCampagne.getAnnonceur() != null) {
            savedCampagne.setAnnonceurId(savedCampagne.getAnnonceur().getId());
        }
        
        return savedCampagne;
    }

    @Override
    public void deleteCampagne(int id) {
        campagneRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public Campagne createCampagneWithProduitAndFormulaire(Campagne campagne, Produit produit, Formulaire formulaire) {
        // Handle the case where annonceurId is set but annonceur object is not
        Integer annonceurId = campagne.getAnnonceurId();
        if (annonceurId != null && campagne.getAnnonceur() == null) {
            annonceurRepository.findById(annonceurId)
                .ifPresent(campagne::setAnnonceur);
        }
        
        // If annonceur is set, make sure it's properly linked
        if (campagne.getAnnonceur() != null) {
            // Make sure the annonceur exists in the database
            Annonceur annonceur = annonceurRepository.findById(campagne.getAnnonceur().getId())
                .orElse(campagne.getAnnonceur());
            
            // Set the annonceur and update the annonceurId
            campagne.setAnnonceur(annonceur);
            campagne.setAnnonceurId(annonceur.getId());
            
            // Add the campaign to the annonceur's list of campaigns
            if (!annonceur.getCampagnesCreees().contains(campagne)) {
                annonceur.getCampagnesCreees().add(campagne);
            }
        }
        
        // Save campagne first
        Campagne savedCampagne = campagneRepository.save(campagne);
        
        // Make sure annonceurId is set for the response
        if (savedCampagne.getAnnonceur() != null) {
            savedCampagne.setAnnonceurId(savedCampagne.getAnnonceur().getId());
        }

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
    @Transactional
    public Campagne updateCampagneWithProduitAndFormulaire(int id, Campagne campagne, Produit produit, Formulaire formulaire) {
        // Find the existing campaign
        Campagne existingCampagne = campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + id));
        
        // Update basic campaign properties
        existingCampagne.setNom(campagne.getNom());
        existingCampagne.setDescription(campagne.getDescription());
        existingCampagne.setDateDebut(campagne.getDateDebut());
        existingCampagne.setDateFin(campagne.getDateFin());
        existingCampagne.setStatut(campagne.getStatut());
        
        // Handle the annonceur relationship
        if (campagne.getAnnonceur() != null) {
            // If a new annonceur is provided, use it
            Annonceur annonceur = annonceurRepository.findById(campagne.getAnnonceur().getId())
                .orElse(campagne.getAnnonceur());
            existingCampagne.setAnnonceur(annonceur);
            existingCampagne.setAnnonceurId(annonceur.getId());
            
            // Add the campaign to the annonceur's list if not already there
            if (!annonceur.getCampagnesCreees().contains(existingCampagne)) {
                annonceur.getCampagnesCreees().add(existingCampagne);
            }
        } else if (campagne.getAnnonceurId() != null) {
            // If only annonceurId is provided, find the annonceur
            Integer annonceurId = campagne.getAnnonceurId();
            if (existingCampagne.getAnnonceur() == null || 
                    !annonceurId.equals(existingCampagne.getAnnonceur().getId())) {
                annonceurRepository.findById(annonceurId)
                    .ifPresent(annonceur -> {
                        existingCampagne.setAnnonceur(annonceur);
                        existingCampagne.setAnnonceurId(annonceur.getId());
                        
                        // Add the campaign to the annonceur's list if not already there
                        if (!annonceur.getCampagnesCreees().contains(existingCampagne)) {
                            annonceur.getCampagnesCreees().add(existingCampagne);
                        }
                    });
            }
        }
        
        // Save updated campaign
        Campagne savedCampagne = campagneRepository.save(existingCampagne);
        
        // Update Produit if it exists, otherwise create a new one
        Produit existingProduit = savedCampagne.getProduit();
        if (existingProduit != null) {
            // Update existing product
            existingProduit.setNom(produit.getNom());
            existingProduit.setDescription(produit.getDescription());
            existingProduit.setCategorie(produit.getCategorie());
            existingProduit.setNoteMoyenne(produit.getNoteMoyenne());
            produitRepository.save(existingProduit);
        } else {
            // Create new product
            produit.setCampagne(savedCampagne);
            Produit savedProduit = produitRepository.save(produit);
            savedCampagne.setProduit(savedProduit);
        }
        
        // Update Formulaire if it exists, otherwise create a new one
        Formulaire existingFormulaire = savedCampagne.getFormulaire();
        if (existingFormulaire != null) {
            // Update existing form (just update the questions)
            existingFormulaire.getFormulaireQuestions().clear();
            if (formulaire.getFormulaireQuestions() != null) {
                formulaire.getFormulaireQuestions().forEach(question -> {
                    question.setFormulaire(existingFormulaire);
                    existingFormulaire.getFormulaireQuestions().add(question);
                });
            }
            formulaireRepository.save(existingFormulaire);
        } else {
            // Create new form
            formulaire.setCampagne(savedCampagne);
            Formulaire savedFormulaire = formulaireRepository.save(formulaire);
            savedCampagne.setFormulaire(savedFormulaire);
        }
        
        // Save the final updated campaign
        Campagne finalCampagne = campagneRepository.save(savedCampagne);
        
        // Make sure annonceurId is set for the response
        if (finalCampagne.getAnnonceur() != null) {
            finalCampagne.setAnnonceurId(finalCampagne.getAnnonceur().getId());
        }
        
        return finalCampagne;
    }
    
    @Override
    public List<Campagne> getCampagnesActives() {
        // Get current date
        Date currentDate = new Date();

        // Find all campaigns and filter active ones
        List<Campagne> activeCampagnes = campagneRepository.findAll().stream()
                .filter(campagne -> "ACTIVE".equals(campagne.getStatut()) ||
                        ("PLANIFIEE".equals(campagne.getStatut()) &&
                                campagne.getDateDebut() != null &&
                                campagne.getDateDebut().before(currentDate) &&
                                (campagne.getDateFin() == null || campagne.getDateFin().after(currentDate))))
                .collect(Collectors.toList());
        
        // Ensure annonceurId is set for all active campaigns
        for (Campagne campagne : activeCampagnes) {
            if (campagne.getAnnonceur() != null) {
                campagne.setAnnonceurId(campagne.getAnnonceur().getId());
            }
        }
        
        return activeCampagnes;
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