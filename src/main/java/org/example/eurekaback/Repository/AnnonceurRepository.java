package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Annonceur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnnonceurRepository extends JpaRepository<Annonceur, Integer> {
    Optional<Annonceur> findByEmail(String email);
    
    @Query("SELECT a FROM Annonceur a LEFT JOIN FETCH a.campagnesCreees WHERE a.id = :id")
    Optional<Annonceur> findByIdWithCampaigns(@Param("id") Integer id);
}
