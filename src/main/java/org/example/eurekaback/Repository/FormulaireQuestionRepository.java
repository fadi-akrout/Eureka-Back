package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FormulaireQuestionRepository extends JpaRepository<FormulaireQuestion, Integer> {
    @Query("SELECT fq FROM FormulaireQuestion fq JOIN FETCH fq.question q LEFT JOIN FETCH q.options WHERE fq.formulaire = :formulaire")
    List<FormulaireQuestion> findByFormulaire(@Param("formulaire") Formulaire formulaire);
    
    @Query("SELECT fq FROM FormulaireQuestion fq JOIN FETCH fq.question q LEFT JOIN FETCH q.options WHERE fq.formulaire.id = :formulaireId")
    List<FormulaireQuestion> findByFormulaireId(@Param("formulaireId") int formulaireId);
    
    @Modifying
    @Query(value = "DELETE FROM formulaire_question WHERE formulaire_id = :formulaireId", nativeQuery = true)
    void deleteByFormulaireId(@Param("formulaireId") int formulaireId);
}
