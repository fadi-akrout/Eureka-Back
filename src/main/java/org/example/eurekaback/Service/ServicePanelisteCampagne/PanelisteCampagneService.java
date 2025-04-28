package org.example.eurekaback.Service.ServicePanelisteCampagne;

import org.example.eurekaback.Entity.PanelisteCampagne;

import java.util.List;
import java.util.Optional;

public interface PanelisteCampagneService {
    List<PanelisteCampagne> getAllPanelisteCampagnes();
    Optional<PanelisteCampagne> getPanelisteCampagneById(int id);
    PanelisteCampagne createPanelisteCampagne(PanelisteCampagne panelisteCampagne);
    void deletePanelisteCampagne(int id);
}
