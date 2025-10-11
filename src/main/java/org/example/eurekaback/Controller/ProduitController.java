package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Produit;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.ProduitRepository;
import org.example.eurekaback.Service.ServiceProduit.ProduitService;
import org.example.eurekaback.Service.ServiceAI.CampaignProductAIService;
import org.example.eurekaback.Dto.AIGenerationResponseDTO;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produits")
@AllArgsConstructor
public class ProduitController {

    private final ProduitService produitService;
    private final CampagneRepository campagneRepository;
    private final ProduitRepository produitRepository;
    private final CampaignProductAIService campaignProductAIService;

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
    
    // ==================== AI INTEGRATION ENDPOINTS ====================
    
    /**
     * Generate AI description for a product
     */
    @PostMapping("/{productId}/generate-description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<?> generateProductDescription(
            @PathVariable int productId,
            @RequestParam(required = false) String keyFeatures,
            @RequestParam(required = false) String additionalContext) {
        
        try {
            // Validate that the product exists
            Produit product = produitRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            
            // Generate and update product description
            Produit updatedProduct = campaignProductAIService.generateAndUpdateProductDescription(
                    productId, keyFeatures, additionalContext);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product description generated successfully");
            response.put("product", updatedProduct);
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error generating product description: " + e.getMessage()));
        }
    }
    
    /**
     * Preview AI-generated product description without saving
     */
    @PostMapping("/preview-description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<?> previewProductDescription(
            @RequestParam String productName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyFeatures,
            @RequestParam(required = false) String additionalContext) {
        
        try {
            AIGenerationResponseDTO response = campaignProductAIService.previewProductDescription(
                    productName, category, keyFeatures, additionalContext);
            
            if (response.isSuccess()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("generatedDescription", response.getGeneratedText());
                
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", response.getErrorMessage()));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error previewing product description: " + e.getMessage()));
        }
    }
}