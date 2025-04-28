package org.example.eurekaback.Service.ServiceFormulaireQuestion;

import org.example.eurekaback.Entity.FormulaireQuestion;

import java.util.List;
import java.util.Optional;

public interface FormulaireQuestionService {
    List<FormulaireQuestion> getAllFormulaireQuestions();
    Optional<FormulaireQuestion> getFormulaireQuestionById(int id);
    FormulaireQuestion createFormulaireQuestion(FormulaireQuestion formulaireQuestion);
    void deleteFormulaireQuestion(int id);
}
