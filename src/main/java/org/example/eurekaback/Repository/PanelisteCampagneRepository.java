package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PanelisteCampagneRepository extends JpaRepository<PanelisteCampagne, Integer> {
    List<PanelisteCampagne> findByPaneliste(Paneliste paneliste);
    List<PanelisteCampagne> findByCampagne(Campagne campagne);
}
