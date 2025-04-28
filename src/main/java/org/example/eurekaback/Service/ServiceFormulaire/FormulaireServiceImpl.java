package org.example.eurekaback.Service.ServiceFormulaire;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.example.eurekaback.Entity.Question;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.FormulaireQuestionRepository;
import org.example.eurekaback.Repository.FormulaireRepository;
import org.example.eurekaback.Repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
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
        return formulaireRepository.findById(id);
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
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        return formulaireRepository.findByCampagne(campagne);
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
}