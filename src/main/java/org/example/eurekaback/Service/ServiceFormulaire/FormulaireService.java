package org.example.eurekaback.Service.ServiceFormulaire;

import org.example.eurekaback.Entity.Formulaire;
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

}

