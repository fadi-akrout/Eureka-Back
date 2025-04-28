package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Formulaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormulaireRepository extends JpaRepository<Formulaire, Integer> {
    Optional<Formulaire> findByCampagne(Campagne campagne);
}
