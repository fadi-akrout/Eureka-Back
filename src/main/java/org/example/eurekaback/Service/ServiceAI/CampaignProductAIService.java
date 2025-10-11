package org.example.eurekaback.Service.ServiceAI;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.AIGenerationRequestDTO;
import org.example.eurekaback.Dto.AIGenerationResponseDTO;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Produit;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class CampaignProductAIService {
    
    private final AITextGenerationService aiTextGenerationService;
    private final CampagneRepository campagneRepository;
    private final ProduitRepository produitRepository;
    
    /**
     * Generate and update campaign description using AI
     */
    @Transactional
    public Campagne generateAndUpdateCampaignDescription(int campaignId, String targetAudience, String additionalContext) {
        Optional<Campagne> campaignOpt = campagneRepository.findById(campaignId);
        
        if (campaignOpt.isEmpty()) {
            throw new RuntimeException("Campaign not found with id: " + campaignId);
        }
        
        Campagne campaign = campaignOpt.get();
        
        try {
            // Build request for AI generation
            AIGenerationRequestDTO request = new AIGenerationRequestDTO();
            request.setName(campaign.getNom());
            request.setCategory(campaign.getProduit() != null ? campaign.getProduit().getCategorie() : null);
            request.setTargetAudience(targetAudience);
            request.setAdditionalContext(additionalContext);
            
            // Generate description using AI
            AIGenerationResponseDTO response = aiTextGenerationService.generateCampaignDescription(request);
            
            if (response.isSuccess()) {
                // Update campaign description
                campaign.setDescription(response.getGeneratedText());
                campaign = campagneRepository.save(campaign);
                
                log.info("Successfully updated campaign {} description using AI", campaignId);
            } else {
                log.error("Failed to generate campaign description: {}", response.getErrorMessage());
                throw new RuntimeException("AI generation failed: " + response.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Error generating campaign description for campaign {}: {}", campaignId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate campaign description: " + e.getMessage());
        }
        
        return campaign;
    }
    
    /**
     * Generate and update product description using AI
     */
    @Transactional
    public Produit generateAndUpdateProductDescription(int productId, String keyFeatures, String additionalContext) {
        Optional<Produit> productOpt = produitRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        Produit product = productOpt.get();
        
        try {
            // Build request for AI generation
            AIGenerationRequestDTO request = new AIGenerationRequestDTO();
            request.setName(product.getNom());
            request.setCategory(product.getCategorie());
            request.setKeyFeatures(keyFeatures);
            request.setAdditionalContext(additionalContext);
            
            // Generate description using AI
            AIGenerationResponseDTO response = aiTextGenerationService.generateProductDescription(request);
            
            if (response.isSuccess()) {
                // Update product description
                product.setDescription(response.getGeneratedText());
                product = produitRepository.save(product);
                
                log.info("Successfully updated product {} description using AI", productId);
            } else {
                log.error("Failed to generate product description: {}", response.getErrorMessage());
                throw new RuntimeException("AI generation failed: " + response.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Error generating product description for product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate product description: " + e.getMessage());
        }
        
        return product;
    }
    
    /**
     * Generate descriptions for both campaign and its associated product
     */
    @Transactional
    public Campagne generateCampaignAndProductDescriptions(int campaignId, String targetAudience, 
                                                          String keyFeatures, String additionalContext) {
        Campagne campaign = generateAndUpdateCampaignDescription(campaignId, targetAudience, additionalContext);
        
        if (campaign.getProduit() != null) {
            generateAndUpdateProductDescription(campaign.getProduit().getId(), keyFeatures, additionalContext);
            // Refresh campaign to get updated product
            campaign = campagneRepository.findById(campaignId).orElse(campaign);
        }
        
        return campaign;
    }
    
    /**
     * Preview generated campaign description without saving
     */
    public AIGenerationResponseDTO previewCampaignDescription(String campaignName, String category, 
                                                            String targetAudience, String additionalContext) {
        AIGenerationRequestDTO request = new AIGenerationRequestDTO();
        request.setName(campaignName);
        request.setCategory(category);
        request.setTargetAudience(targetAudience);
        request.setAdditionalContext(additionalContext);
        
        return aiTextGenerationService.generateCampaignDescription(request);
    }
    
    /**
     * Preview generated product description without saving
     */
    public AIGenerationResponseDTO previewProductDescription(String productName, String category, 
                                                           String keyFeatures, String additionalContext) {
        AIGenerationRequestDTO request = new AIGenerationRequestDTO();
        request.setName(productName);
        request.setCategory(category);
        request.setKeyFeatures(keyFeatures);
        request.setAdditionalContext(additionalContext);
        
        return aiTextGenerationService.generateProductDescription(request);
    }
}