package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Formulaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FormulaireRepository extends JpaRepository<Formulaire, Integer> {
    Optional<Formulaire> findByCampagne(Campagne campagne);
    
    @Query("SELECT DISTINCT f FROM Formulaire f " +
           "LEFT JOIN FETCH f.formulaireQuestions fq " +
           "WHERE f.id = :id")
    Optional<Formulaire> findByIdWithQuestions(@Param("id") int id);
    
    @Query("SELECT DISTINCT f FROM Formulaire f " +
           "LEFT JOIN FETCH f.formulaireQuestions fq " +
           "LEFT JOIN FETCH fq.question q " +
           "LEFT JOIN FETCH q.options " +
           "WHERE f.id = :id")
    Optional<Formulaire> findByIdWithQuestionsAndOptions(@Param("id") int id);
    
    @Query("SELECT DISTINCT f FROM Formulaire f " +
           "LEFT JOIN FETCH f.formulaireQuestions fq " +
           "WHERE f.campagne.id = :campagneId")
    Optional<Formulaire> findByCampagneIdWithQuestions(@Param("campagneId") int campagneId);
    
    @Query("SELECT DISTINCT f FROM Formulaire f " +
           "LEFT JOIN FETCH f.formulaireQuestions fq " +
           "LEFT JOIN FETCH fq.question q " +
           "LEFT JOIN FETCH q.options " +
           "WHERE f.campagne.id = :campagneId")
    Optional<Formulaire> findByCampagneIdWithQuestionsAndOptions(@Param("campagneId") int campagneId);
}
