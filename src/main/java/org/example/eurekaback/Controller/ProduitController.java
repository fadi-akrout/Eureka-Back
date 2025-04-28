package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Produit;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.ProduitRepository;
import org.example.eurekaback.Service.ServiceProduit.ProduitService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/produits")
@AllArgsConstructor
public class ProduitController {

    private final ProduitService produitService;
    private final CampagneRepository campagneRepository;
    private final ProduitRepository produitRepository;

    @GetMapping
    public ResponseEntity<List<Produit>> getAllProduits() {
        return ResponseEntity.ok(produitService.getAllProduits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produit> getProduitById(@PathVariable int id) {
        return produitService.getProduitById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Produit not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Produit> createProduit(@Valid @RequestBody Produit produit) {
        return new ResponseEntity<>(produitService.createProduit(produit), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable int id) {
        produitService.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/campagne/{campagneId}")
    public ResponseEntity<Produit> getProduitByCampagne(@PathVariable int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + campagneId));

        return  produitRepository.findByCampagne(campagne)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Produit not found for campagne with id: " + campagneId));
    }
}