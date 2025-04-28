package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Service.ServicePanelisteCampagne.PanelisteCampagneService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/paneliste-campagnes")
@AllArgsConstructor
public class PanelisteCampagneController {

    private final PanelisteCampagneService panelisteCampagneService;
    private final PanelisteRepository panelisteRepository;
    private final CampagneRepository campagneRepository;
    private final PanelisteCampagneRepository panelisteCampagneRepository;

    @GetMapping
    public ResponseEntity<List<PanelisteCampagne>> getAllPanelisteCampagnes() {
        return ResponseEntity.ok(panelisteCampagneService.getAllPanelisteCampagnes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PanelisteCampagne> getPanelisteCampagneById(@PathVariable int id) {
        return panelisteCampagneService.getPanelisteCampagneById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("PanelisteCampagne not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<PanelisteCampagne> createPanelisteCampagne(@Valid @RequestBody PanelisteCampagne panelisteCampagne) {
        return new ResponseEntity<>(panelisteCampagneService.createPanelisteCampagne(panelisteCampagne), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePanelisteCampagne(@PathVariable int id) {
        panelisteCampagneService.deletePanelisteCampagne(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paneliste/{panelisteId}")
    public ResponseEntity<List<PanelisteCampagne>> getPanelisteCampagnesByPaneliste(@PathVariable int panelisteId) {
        Paneliste paneliste = panelisteRepository.findById(panelisteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paneliste not found with id: " + panelisteId));

        return ResponseEntity.ok(panelisteCampagneRepository.findByPaneliste(paneliste));
    }

    @GetMapping("/campagne/{campagneId}")
    public ResponseEntity<List<PanelisteCampagne>> getPanelisteCampagnesByCampagne(@PathVariable int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + campagneId));

        return ResponseEntity.ok(panelisteCampagneRepository.findByCampagne(campagne));
    }
}