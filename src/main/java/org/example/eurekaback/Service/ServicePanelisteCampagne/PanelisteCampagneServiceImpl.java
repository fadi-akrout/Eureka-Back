package org.example.eurekaback.Service.ServicePanelisteCampagne;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PanelisteCampagneServiceImpl implements PanelisteCampagneService {
    private final PanelisteCampagneRepository panelisteCampagneRepository;

    @Override
    public List<PanelisteCampagne> getAllPanelisteCampagnes() {
        return panelisteCampagneRepository.findAll();
    }

    @Override
    public Optional<PanelisteCampagne> getPanelisteCampagneById(int id) {
        return panelisteCampagneRepository.findById(id);
    }

    @Override
    public PanelisteCampagne createPanelisteCampagne(PanelisteCampagne panelisteCampagne) {
        return panelisteCampagneRepository.save(panelisteCampagne);
    }

    @Override
    public void deletePanelisteCampagne(int id) {
        panelisteCampagneRepository.deleteById(id);
    }
}
