package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Campagne;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampagneRepository extends JpaRepository<Campagne, Integer> {
}
