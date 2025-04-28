package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Annonceur;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AnnonceurRepository extends JpaRepository<Annonceur, Integer> {
}
