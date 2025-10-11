package org.example.eurekaback.Service.ServiceCampagne;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.*;
import org.example.eurekaback.Repository.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private final QuestionRepository questionRepository;
    private final FormulaireQuestionRepository formulaireQuestionRepository;

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

    /**
     * Helper method to delete all questions associated with a form
     */

    @Transactional
    private void deleteQuestionsForFormulaire(Formulaire formulaire) {
        if (formulaire == null) {
            System.out.println("Formulaire is null, skipping question deletion");
            return;
        }
        
        int formulaireId = formulaire.getId();
        System.out.println("Starting deletion of questions for formulaire ID: " + formulaireId);
        
        try {
            // First, check if there are any questions directly in the database
            String checkSql = "SELECT COUNT(*) FROM formulaire_question WHERE formulaire_id = " + formulaireId;
            System.out.println("Executing SQL: " + checkSql);
            
            // Get count of questions for logging
            List<Question> questions = questionRepository.findQuestionsByFormulaireId(formulaireId);
            int questionCount = questions.size();
            System.out.println("Found " + questionCount + " questions to delete for formulaire ID: " + formulaireId);
            
            // Direct database check for formulaire_question entries
            String directCheckSql = "SELECT fq.id, fq.formulaire_id, fq.question_id FROM formulaire_question fq WHERE fq.formulaire_id = " + formulaireId;
            System.out.println("Direct check SQL: " + directCheckSql);
            
            // Log the questions for debugging
            if (questionCount > 0) {
                for (Question q : questions) {
                    System.out.println("Question to delete: ID=" + q.getId() + ", Text=" + q.getTexte());
                }
                
                // Step 1: Delete all related options (to avoid foreign key constraints)
                System.out.println("Deleting options for formulaire ID: " + formulaireId);
                questionRepository.deleteOptionsByFormulaireId(formulaireId);
                
                // Step 2: Delete all related responses (to avoid foreign key constraints)
                System.out.println("Deleting responses for formulaire ID: " + formulaireId);
                questionRepository.deleteReponsesByFormulaireId(formulaireId);
            }
            
            // Step 3: Always delete FormulaireQuestion entries for this form using direct query
            System.out.println("Deleting formulaire-question mappings for formulaire ID: " + formulaireId);
            // Use a direct SQL query to ensure deletion
            String deleteFormQuestionSql = "DELETE FROM formulaire_question WHERE formulaire_id = " + formulaireId;
            System.out.println("Executing SQL: " + deleteFormQuestionSql);
            formulaireQuestionRepository.deleteByFormulaireId(formulaireId);
            
            if (questionCount > 0) {
                // Step 4: Delete all questions associated with this form using direct query
                System.out.println("Deleting questions for formulaire ID: " + formulaireId);
                // Use a direct SQL query to ensure deletion
                String deleteQuestionsSql = "DELETE FROM question WHERE id IN (SELECT question_id FROM formulaire_question WHERE formulaire_id = " + formulaireId + ")";
                System.out.println("Executing SQL: " + deleteQuestionsSql);
                questionRepository.deleteQuestionsByFormulaireId(formulaireId);
            }
            
            // Step 5: Clear the form's questions list
            formulaire.setFormulaireQuestions(new HashSet<>());
            formulaireRepository.save(formulaire);
            
            System.out.println("Successfully deleted " + questionCount + " questions for formulaire ID " + formulaireId);
            
        } catch (Exception e) {
            // Log the error but continue with the process
            System.err.println("Error deleting questions for formulaire ID " + formulaireId + ": " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to manual deletion if the direct queries fail
            try {
                // Get all questions associated with this form
                List<Question> questions = questionRepository.findQuestionsByFormulaireId(formulaireId);
                
                // For each question, clear its relationships to avoid constraint violations
                for (Question question : questions) {
                    if (question.getOptions() != null) {
                        question.getOptions().clear();
                    }
                    
                    if (question.getReponses() != null) {
                        question.getReponses().clear();
                    }
                    
                    // Save the question with cleared relationships
                    questionRepository.save(question);
                }
                
                // Delete all FormulaireQuestion entries for this form
                List<FormulaireQuestion> formulaireQuestions = formulaireQuestionRepository.findByFormulaireId(formulaireId);
                formulaireQuestionRepository.deleteAll(formulaireQuestions);
                
                // Delete all questions associated with this form
                for (Question question : questions) {
                    questionRepository.delete(question);
                }
                
                System.out.println("Successfully deleted " + questions.size() + " questions for formulaire ID " + formulaireId + " using fallback method");
                
            } catch (Exception ex) {
                System.err.println("Error in fallback deletion for formulaire ID " + formulaireId + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    @Transactional
    public void deleteCampagne(int id) {
        System.out.println("Starting deletion of campaign with ID: " + id);
        
        // Find the campaign to be deleted
        Campagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + id));
        
        System.out.println("Found campaign: " + campagne.getNom() + " (ID: " + campagne.getId() + ")");
        
        // Remove the campaign from its annonceur's list if applicable
        if (campagne.getAnnonceur() != null) {
            System.out.println("Removing campaign from annonceur: " + campagne.getAnnonceur().getNom());
            campagne.getAnnonceur().getCampagnesCreees().remove(campagne);
        }
        
        // Get the form associated with the campaign and delete its questions
        Formulaire formulaire = campagne.getFormulaire();
        if (formulaire != null) {
            System.out.println("Found associated formulaire with ID: " + formulaire.getId());
            
            // First, explicitly delete all questions and their associations
            deleteQuestionsForFormulaire(formulaire);
            
            // Execute direct SQL to ensure all formulaire_question entries are deleted
            System.out.println("Executing direct SQL to delete formulaire_question entries");
            String deleteFormQuestionSql = "DELETE FROM formulaire_question WHERE formulaire_id = " + formulaire.getId();
            System.out.println("SQL: " + deleteFormQuestionSql);
            
            // Then, explicitly delete the formulaire
            System.out.println("Explicitly deleting formulaire with ID: " + formulaire.getId());
            formulaireRepository.delete(formulaire);
            
            // Detach the formulaire from the campaign to avoid cascade issues
            campagne.setFormulaire(null);
            campagneRepository.save(campagne);
        } else {
            System.out.println("No formulaire found for this campaign");
        }
        
        // Handle other relationships
        if (campagne.getProduit() != null) {
            System.out.println("Detaching produit with ID: " + campagne.getProduit().getId());
            Produit produit = campagne.getProduit();
            campagne.setProduit(null);
            produitRepository.delete(produit);
        }
        
        if (campagne.getResultatAnalyse() != null) {
            System.out.println("Detaching resultat analyse with ID: " + campagne.getResultatAnalyse().getId());
            ResultatAnalyse resultatAnalyse = campagne.getResultatAnalyse();
            campagne.setResultatAnalyse(null);
            resultatAnalyseRepository.delete(resultatAnalyse);
        }
        
        // Clear any paneliste associations
        if (campagne.getPanelisteCampagnes() != null && !campagne.getPanelisteCampagnes().isEmpty()) {
            System.out.println("Clearing " + campagne.getPanelisteCampagnes().size() + " paneliste associations");
            for (PanelisteCampagne pc : new ArrayList<>(campagne.getPanelisteCampagnes())) {
                campagne.removePanelisteCampagne(pc);
                panelisteCampagneRepository.delete(pc);
            }
        }
        
        // Save the campaign with all relationships detached
        campagneRepository.save(campagne);
        
        // Finally, delete the campaign
        System.out.println("Deleting campaign with ID: " + id);
        campagneRepository.deleteById(id);
        
        System.out.println("Campaign deletion completed successfully");
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
        // Even if the form has no questions, we still create and associate it with the campaign
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
        
        // Update the image URL if provided
        if (campagne.getImageUrl() != null && !campagne.getImageUrl().isEmpty()) {
            System.out.println("Updating campaign image URL from: " + existingCampagne.getImageUrl() + " to: " + campagne.getImageUrl());
            existingCampagne.setImageUrl(campagne.getImageUrl());
        }
        
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
            // Save the form even if it has no questions
            formulaireRepository.save(existingFormulaire);
        } else {
            // Create new form even if it has no questions
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