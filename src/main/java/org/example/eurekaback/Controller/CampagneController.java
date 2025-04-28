package org.example.eurekaback.Controller;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.eurekaback.Entity.*;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Service.ServiceCampagne.CampagneService;
import org.example.eurekaback.Service.ServicePaneliste.PanelisteService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campagnes")
@AllArgsConstructor
public class CampagneController {

    private final CampagneService campagneService;
    private final PanelisteService panelisteService;
    private final CampagneRepository campagneRepository;
    // private final PanelisteService panelisteService;

    @GetMapping
    public ResponseEntity<List<Campagne>> getAllCampagnes() {
        return ResponseEntity.ok(campagneService.getAllCampagnes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campagne> getCampagneById(@PathVariable int id) {
        return campagneService.getCampagneById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Campagne> createCampagne(@Valid @RequestBody Campagne campagne) {
        return new ResponseEntity<>(campagneService.createCampagne(campagne), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampagne(@PathVariable int id) {
        campagneService.deleteCampagne(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/actives")
    public ResponseEntity<List<Campagne>> getCampagnesActives() {
        return ResponseEntity.ok(campagneService.getCampagnesActives());
    }

    @PostMapping("/complete")
    public ResponseEntity<Campagne> createCampagneComplete(@RequestBody CampagneCompleteRequest request) {
        Campagne createdCampagne = campagneService.createCampagneWithProduitAndFormulaire(
                request.getCampagne(), request.getProduit(), request.getFormulaire());

        // Fetch the complete campaign with associations
        Campagne completeCampagne = campagneRepository.findById(createdCampagne.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found"));

        return new ResponseEntity<>(completeCampagne, HttpStatus.CREATED);
    }

    @PostMapping("/{campagneId}/panelistes")
    public ResponseEntity<Void> assignerPanelistes(
            @PathVariable int campagneId,
            @RequestBody List<Integer> panelisteIds) {

        Campagne campagne = campagneService.getCampagneById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + campagneId));

        // Convert IDs to Paneliste objects (you'll need to inject PanelisteService)
        List<Paneliste> panelistes = panelisteIds.stream()
                .map(id ->  panelisteService.getPanelisteById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Paneliste not found with id: " + id)))
                .collect(Collectors.toList());

        campagneService.assignerPanelistesACampagne(campagne, panelistes);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/demarrer")
    public ResponseEntity<Void> demarrerCampagne(@PathVariable int id) {
        campagneService.demarrerCampagne(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/terminer")
    public ResponseEntity<Void> terminerCampagne(@PathVariable int id) {
        campagneService.terminerCampagne(id);
        return ResponseEntity.noContent().build();
    }
}

@Data
class CampagneCompleteRequest {
    @NotNull
    private Campagne campagne;
    @NotNull
    private Produit produit;
    @NotNull
    private Formulaire formulaire;
}