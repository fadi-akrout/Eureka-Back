package org.example.eurekaback.Controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Service.ServiceAnnonceur.AnnonceurService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annonceurs")
@AllArgsConstructor
public class AnnonceurController {

    private final AnnonceurService annonceurService;

    @GetMapping
    public ResponseEntity<List<Annonceur>> getAllAnnonceurs() {
        return ResponseEntity.ok(annonceurService.getAllAnnonceurs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Annonceur> getAnnonceurById(@PathVariable int id) {
        return annonceurService.getAnnonceurById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Annonceur not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Annonceur> createAnnonceur(@Valid @RequestBody Annonceur annonceur) {
        return new ResponseEntity<>(annonceurService.createAnnonceur(annonceur), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnonceur(@PathVariable int id) {
        annonceurService.deleteAnnonceur(id);
        return ResponseEntity.noContent().build();
    }
}