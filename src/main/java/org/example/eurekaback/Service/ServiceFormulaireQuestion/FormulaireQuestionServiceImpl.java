package org.example.eurekaback.Service.ServiceFormulaireQuestion;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.example.eurekaback.Repository.FormulaireQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor

public class FormulaireQuestionServiceImpl implements FormulaireQuestionService {
    private final FormulaireQuestionRepository formulaireQuestionRepository;

    @Override
    public List<FormulaireQuestion> getAllFormulaireQuestions() {
        return formulaireQuestionRepository.findAll();
    }

    @Override
    public Optional<FormulaireQuestion> getFormulaireQuestionById(int id) {
        return formulaireQuestionRepository.findById(id);
    }

    @Override
    public FormulaireQuestion createFormulaireQuestion(FormulaireQuestion formulaireQuestion) {
        return formulaireQuestionRepository.save(formulaireQuestion);
    }

    @Override
    public void deleteFormulaireQuestion(int id) {
        formulaireQuestionRepository.deleteById(id);
    }
}