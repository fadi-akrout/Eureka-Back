package org.example.eurekaback.Service.ServiceFormulaire;

import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.example.eurekaback.Entity.Question;

import java.util.List;
import java.util.Optional;

public interface FormulaireService {
    List<Formulaire> getAllFormulaires();
    Optional<Formulaire> getFormulaireById(int id);
    Formulaire createFormulaire(Formulaire formulaire);
    void deleteFormulaire(int id);

    Optional<Formulaire> getFormulaireByCampagne(int campagneId);
    Formulaire createFormulaireWithQuestions(Formulaire formulaire, List<Question> questions);
    
    /**
     * Add questions to an existing form
     * @param formId The ID of the form to add questions to
     * @param questions The list of questions to add
     * @return The updated form with the new questions
     */
    Formulaire addQuestionsToForm(int formId, List<Question> questions);
    
    /**
     * Get all questions associated with a form
     * @param formId The ID of the form
     * @return List of FormulaireQuestion objects containing the form-question associations
     */
    List<FormulaireQuestion> getQuestionsForForm(int formId);
    
    /**
     * Get formulaire with all its questions and options
     * @param id The ID of the formulaire
     * @return Optional containing the formulaire with questions and options
     */
    Optional<Formulaire> getFormulaireWithQuestions(int id);
}

