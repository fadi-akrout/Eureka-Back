package org.example.eurekaback.Service.ServiceFormulaire;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.example.eurekaback.Entity.Question;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.FormulaireQuestionRepository;
import org.example.eurekaback.Repository.FormulaireRepository;
import org.example.eurekaback.Repository.QuestionRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class FormulaireServiceImpl implements FormulaireService {
    private final FormulaireRepository formulaireRepository;
    private final CampagneRepository campagneRepository;
    private final QuestionRepository questionRepository;
    private final FormulaireQuestionRepository formulaireQuestionRepository;

    @Override
    public List<Formulaire> getAllFormulaires() {
        return formulaireRepository.findAll();
    }

    @Override
    public Optional<Formulaire> getFormulaireById(int id) {
        // Try to get the form with questions and options
        Optional<Formulaire> formulaireWithQuestions = formulaireRepository.findByIdWithQuestionsAndOptions(id);
        
        // If found, return it, otherwise fall back to the standard findById
        return formulaireWithQuestions.isPresent() ? formulaireWithQuestions : formulaireRepository.findById(id);
    }

    @Override
    public Formulaire createFormulaire(Formulaire formulaire) {
        return formulaireRepository.save(formulaire);
    }

    @Override
    public void deleteFormulaire(int id) {
        formulaireRepository.deleteById(id);
    }
    
    @Override
    public Optional<Formulaire> getFormulaireByCampagne(int campagneId) {
        // Check if campagne exists
        if (!campagneRepository.existsById(campagneId)) {
            throw new RuntimeException("Campagne not found with id: " + campagneId);
        }
        
        // Try to get the form with questions and options
        Optional<Formulaire> formulaireWithQuestions = formulaireRepository.findByCampagneIdWithQuestionsAndOptions(campagneId);
        
        // If found, return it, otherwise fall back to the standard findByCampagne
        if (formulaireWithQuestions.isPresent()) {
            return formulaireWithQuestions;
        } else {
            Campagne campagne = campagneRepository.findById(campagneId).orElseThrow();
            return formulaireRepository.findByCampagne(campagne);
        }
    }

    @Override
    @Transactional
    public Formulaire createFormulaireWithQuestions(Formulaire formulaire, List<Question> questions) {
        // Save the form first
        Formulaire savedFormulaire = formulaireRepository.save(formulaire);

        // Save questions if they don't exist yet
        for (Question question : questions) {
            Question savedQuestion;
            if (question.getId() == 0) {
                savedQuestion = questionRepository.save(question);
            } else {
                savedQuestion = question;
            }

            // Create the association
            FormulaireQuestion formulaireQuestion = new FormulaireQuestion();
            formulaireQuestion.setFormulaire(savedFormulaire);
            formulaireQuestion.setQuestion(savedQuestion);
            formulaireQuestionRepository.save(formulaireQuestion);
        }

        return savedFormulaire;
    }
    
    @Override
    @Transactional
    public Formulaire addQuestionsToForm(int formId, List<Question> questions) {
        // Find the form
        Formulaire formulaire = formulaireRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with id: " + formId));
        
        // Save questions and create associations
        for (Question question : questions) {
            // Save the question
            Question savedQuestion = questionRepository.save(question);
            
            // Save options if present
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                question.getOptions().forEach(option -> {
                    option.setQuestion(savedQuestion);
                });
                // Options will be saved via cascade
            }
            
            // Create and save the form-question association
            FormulaireQuestion formulaireQuestion = new FormulaireQuestion();
            formulaireQuestion.setFormulaire(formulaire);
            formulaireQuestion.setQuestion(savedQuestion);
            formulaireQuestionRepository.save(formulaireQuestion);
        }
        
        // Return the updated form
        return formulaireRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found after adding questions"));
    }
    
    @Override
    public List<FormulaireQuestion> getQuestionsForForm(int formId) {
        // Find the form
        Formulaire formulaire = formulaireRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with id: " + formId));
        
        // Get all FormulaireQuestion associations for this form
        return formulaireQuestionRepository.findByFormulaire(formulaire);
    }
    
    @Override
    public Optional<Formulaire> getFormulaireWithQuestions(int id) {
        log.info("Fetching formulaire with questions for id: {}", id);
        Optional<Formulaire> result = formulaireRepository.findByIdWithQuestionsAndOptions(id);
        if (result.isPresent()) {
            Formulaire formulaire = result.get();
            log.info("Found formulaire with id: {}, questions count: {}", 
                    formulaire.getId(), 
                    formulaire.getFormulaireQuestions() != null ? formulaire.getFormulaireQuestions().size() : 0);
        } else {
            log.warn("No formulaire found with id: {}", id);
        }
        return result;
    }
}