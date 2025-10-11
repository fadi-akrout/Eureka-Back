package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    
    @Query(value = "SELECT q.* FROM question q JOIN formulaire_question fq ON q.id = fq.question_id WHERE fq.formulaire_id = :formulaireId", nativeQuery = true)
    List<Question> findQuestionsByFormulaireId(@Param("formulaireId") int formulaireId);
    
    @Query(value = "SELECT COUNT(*) > 0 FROM formulaire_question WHERE formulaire_id = :formulaireId", nativeQuery = true)
    boolean hasQuestionsForFormulaire(@Param("formulaireId") int formulaireId);
    
    @Modifying
    @Query(value = "DELETE FROM question WHERE id IN (SELECT question_id FROM formulaire_question WHERE formulaire_id = :formulaireId)", nativeQuery = true)
    void deleteQuestionsByFormulaireId(@Param("formulaireId") int formulaireId);
    
    @Modifying
    @Query(value = "DELETE FROM \"option\" WHERE question_id IN (SELECT question_id FROM formulaire_question WHERE formulaire_id = :formulaireId)", nativeQuery = true)
    void deleteOptionsByFormulaireId(@Param("formulaireId") int formulaireId);
    
    @Modifying
    @Query(value = "DELETE FROM reponse WHERE question_id IN (SELECT question_id FROM formulaire_question WHERE formulaire_id = :formulaireId)", nativeQuery = true)
    void deleteReponsesByFormulaireId(@Param("formulaireId") int formulaireId);
}
