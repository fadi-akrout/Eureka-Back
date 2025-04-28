package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulaireQuestionRepository extends JpaRepository<FormulaireQuestion, Integer> {
    List<FormulaireQuestion> findByFormulaire(Formulaire formulaire);
}
