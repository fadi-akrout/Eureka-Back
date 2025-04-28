package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Service.ServicePaneliste.PanelisteService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/panelistes")
@AllArgsConstructor
public class PanelisteController {

    private final PanelisteService panelisteService;

    @GetMapping
    public ResponseEntity<List<Paneliste>> getAllPanelistes() {
        return ResponseEntity.ok(panelisteService.getAllPanelistes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paneliste> getPanelisteById(@PathVariable int id) {
        return panelisteService.getPanelisteById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Paneliste not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Paneliste> createPaneliste(@Valid @RequestBody Paneliste paneliste) {
        return new ResponseEntity<>(panelisteService.createPaneliste(paneliste), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaneliste(@PathVariable int id) {
        panelisteService.deletePaneliste(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/campagnes")
    public ResponseEntity<List<Campagne>> getCampagnesParticipees(@PathVariable int id) {
        return ResponseEntity.ok(panelisteService.getCampagnesParticipees(id));
    }

    @PostMapping("/{panelisteId}/participer/{campagneId}")
    public ResponseEntity<Void> participerACampagne(
            @PathVariable int panelisteId,
            @PathVariable int campagneId) {
        panelisteService.participerACampagne(panelisteId, campagneId);
        return ResponseEntity.noContent().build();
    }
}