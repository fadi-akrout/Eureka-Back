package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Integer> {
    Optional<Produit> findByCampagne(Campagne campagne);
}
