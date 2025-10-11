package org.example.eurekaback.Controller;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.eurekaback.Dto.ParticipationStatusDTO;
import org.example.eurekaback.Entity.*;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Service.FileStorageService;
import org.example.eurekaback.Service.ServiceCampagne.CampagneService;
import org.example.eurekaback.Service.ServiceFormulaire.FormulaireService;
import org.example.eurekaback.Service.ServicePaneliste.PanelisteService;
import org.example.eurekaback.Service.ServiceParticipationRequest.ParticipationRequestService;
import org.example.eurekaback.Service.ServiceAI.CampaignProductAIService;
import org.example.eurekaback.Dto.AIGenerationResponseDTO;
import org.example.eurekaback.Dto.ParticipationRequestDTO;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campagnes")
@AllArgsConstructor
public class CampagneController {

    private final CampagneService campagneService;
    private final PanelisteService panelisteService;
    private final ParticipationRequestService participationRequestService;
    private final CampagneRepository campagneRepository;
    private final AnnonceurRepository annonceurRepository;
    private final FormulaireService formulaireService;
    private final FileStorageService fileStorageService;
    private final CampaignProductAIService campaignProductAIService;

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
        // Get the currently authenticated annonceur
        Annonceur annonceur = getCurrentAnnonceur();
        if (annonceur != null) {
            // Set the annonceur for the campaign
            campagne.setAnnonceur(annonceur);
            // Also set the annonceurId for consistency
            campagne.setAnnonceurId(annonceur.getId());
        }
        
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
        // Get the currently authenticated annonceur
        Annonceur annonceur = getCurrentAnnonceur();
        if (annonceur != null) {
            // Set the annonceur for the campaign
            request.getCampagne().setAnnonceur(annonceur);
            // Also set the annonceurId for consistency
            request.getCampagne().setAnnonceurId(annonceur.getId());
        }
        
        Campagne createdCampagne = campagneService.createCampagneWithProduitAndFormulaire(
                request.getCampagne(), request.getProduit(), request.getFormulaire());

        // Fetch the complete campaign with associations
        Campagne completeCampagne = campagneRepository.findById(createdCampagne.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found"));
        
        // Ensure annonceurId is set for the response
        if (completeCampagne.getAnnonceur() != null) {
            completeCampagne.setAnnonceurId(completeCampagne.getAnnonceur().getId());
        }

        return new ResponseEntity<>(completeCampagne, HttpStatus.CREATED);
    }
    
    /**
     * Create a complete campaign with an image
     * 
     * @param campagneJson The campaign data as JSON
     * @param produitJson The product data as JSON
     * @param formulaireJson The form data as JSON
     * @param image The campaign image file
     * @return The created campaign
     */
    @PostMapping(value = "/complete-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCampagneWithImage(
            @RequestPart("campagne") String campagneJson,
            @RequestPart("produit") String produitJson,
            @RequestPart("formulaire") String formulaireJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        try {
            // Parse JSON strings to objects
            Campagne campagne = new com.fasterxml.jackson.databind.ObjectMapper().readValue(campagneJson, Campagne.class);
            Produit produit = new com.fasterxml.jackson.databind.ObjectMapper().readValue(produitJson, Produit.class);
            Formulaire formulaire = new com.fasterxml.jackson.databind.ObjectMapper().readValue(formulaireJson, Formulaire.class);
            
            // Get the currently authenticated annonceur
            Annonceur annonceur = getCurrentAnnonceur();
            if (annonceur != null) {
                // Set the annonceur for the campaign
                campagne.setAnnonceur(annonceur);
                // Also set the annonceurId for consistency
                campagne.setAnnonceurId(annonceur.getId());
            }
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String fileName = fileStorageService.storeFile(image);
                String imageUrl = fileStorageService.getFileUrl(fileName);
                // Ensure the URL doesn't have double slashes
                imageUrl = imageUrl.replaceAll("//+", "/");
                // Make sure the URL is compatible with frontend expectations
                if (!imageUrl.startsWith("/api/images/")) {
                    imageUrl = "/api/images" + imageUrl;
                }
                campagne.setImageUrl(imageUrl);
                System.out.println("New image URL for creation: " + imageUrl);
            }
            
            // Create the campaign with its associated entities
            Campagne createdCampagne = campagneService.createCampagneWithProduitAndFormulaire(
                    campagne, produit, formulaire);
            
            // Fetch the complete campaign with associations
            Campagne completeCampagne = campagneRepository.findById(createdCampagne.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campagne not found"));
            
            // Ensure annonceurId is set for the response
            if (completeCampagne.getAnnonceur() != null) {
                completeCampagne.setAnnonceurId(completeCampagne.getAnnonceur().getId());
            }
            
            return new ResponseEntity<>(completeCampagne, HttpStatus.CREATED);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error processing the request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<Campagne> updateCampagneComplete(@PathVariable int id, @RequestBody CampagneCompleteRequest request) {
        // Log the update request
        System.out.println("Updating complete campaign: " + id + ", Request: " + request);
        
        // Get the existing campaign to check ownership
        Campagne existingCampagne = campagneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + id));
        
        // Get the currently authenticated annonceur
        Annonceur annonceur = getCurrentAnnonceur();
        if (annonceur != null) {
            // Set the annonceur for the campaign being updated
            request.getCampagne().setAnnonceur(annonceur);
            request.getCampagne().setAnnonceurId(annonceur.getId());
        } else if (existingCampagne.getAnnonceur() != null) {
            // Preserve the existing annonceur if not authenticated as an annonceur
            request.getCampagne().setAnnonceur(existingCampagne.getAnnonceur());
            request.getCampagne().setAnnonceurId(existingCampagne.getAnnonceur().getId());
        }
        
        // Preserve the existing image URL if not provided in the request
        if (request.getCampagne().getImageUrl() == null && existingCampagne.getImageUrl() != null) {
            request.getCampagne().setImageUrl(existingCampagne.getImageUrl());
        }
        
        // Update the campaign with its associated product and form
        Campagne updatedCampagne = campagneService.updateCampagneWithProduitAndFormulaire(
                id, request.getCampagne(), request.getProduit(), request.getFormulaire());
        
        // Ensure annonceurId is set in the response
        if (updatedCampagne.getAnnonceur() != null) {
            updatedCampagne.setAnnonceurId(updatedCampagne.getAnnonceur().getId());
        }
        
        return ResponseEntity.ok(updatedCampagne);
    }
    
    /**
     * Update a complete campaign with an image
     * 
     * @param id The ID of the campaign to update
     * @param campagneJson The campaign data as JSON
     * @param produitJson The product data as JSON
     * @param formulaireJson The form data as JSON
     * @param image The campaign image file
     * @return The updated campaign
     */
    @PutMapping(value = "/{id}/complete-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCampagneWithImage(
            @PathVariable int id,
            @RequestPart("campagne") String campagneJson,
            @RequestPart("produit") String produitJson,
            @RequestPart("formulaire") String formulaireJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        try {
            // Parse JSON strings to objects
            Campagne campagne = new com.fasterxml.jackson.databind.ObjectMapper().readValue(campagneJson, Campagne.class);
            Produit produit = new com.fasterxml.jackson.databind.ObjectMapper().readValue(produitJson, Produit.class);
            Formulaire formulaire = new com.fasterxml.jackson.databind.ObjectMapper().readValue(formulaireJson, Formulaire.class);
            
            // Get the existing campaign to check ownership
            Campagne existingCampagne = campagneRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + id));
            
            // Get the currently authenticated annonceur
            Annonceur annonceur = getCurrentAnnonceur();
            if (annonceur != null) {
                // Set the annonceur for the campaign being updated
                campagne.setAnnonceur(annonceur);
                campagne.setAnnonceurId(annonceur.getId());
            } else if (existingCampagne.getAnnonceur() != null) {
                // Preserve the existing annonceur if not authenticated as an annonceur
                campagne.setAnnonceur(existingCampagne.getAnnonceur());
                campagne.setAnnonceurId(existingCampagne.getAnnonceur().getId());
            }
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                System.out.println("Received new image for campaign update: " + image.getOriginalFilename() + ", size: " + image.getSize() + " bytes");
                String fileName = fileStorageService.storeFile(image);
                String imageUrl = fileStorageService.getFileUrl(fileName);
                // Ensure the URL doesn't have double slashes
                imageUrl = imageUrl.replaceAll("//+", "/");
                // Make sure the URL is compatible with frontend expectations
                if (!imageUrl.startsWith("/api/images/")) {
                    imageUrl = "/api/images" + imageUrl;
                }
                campagne.setImageUrl(imageUrl);
                System.out.println("New image URL for update: " + imageUrl);
            } else if (campagne.getImageUrl() == null && existingCampagne.getImageUrl() != null) {
                // Preserve the existing image URL if not provided in the request
                // Ensure the URL doesn't have double slashes
                String normalizedUrl = existingCampagne.getImageUrl().replaceAll("//+", "/");
                // Make sure the URL is compatible with frontend expectations
                if (!normalizedUrl.startsWith("/api/images/")) {
                    normalizedUrl = "/api/images" + normalizedUrl;
                }
                campagne.setImageUrl(normalizedUrl);
                System.out.println("Preserved image URL: " + normalizedUrl);
            } else {
                System.out.println("No new image provided. Current imageUrl in request: " + 
                    (campagne.getImageUrl() != null ? campagne.getImageUrl() : "null") + 
                    ", Existing imageUrl: " + 
                    (existingCampagne.getImageUrl() != null ? existingCampagne.getImageUrl() : "null"));
            }
            
            // Update the campaign with its associated product and form
            Campagne updatedCampagne = campagneService.updateCampagneWithProduitAndFormulaire(
                    id, campagne, produit, formulaire);
            
            // Ensure annonceurId is set in the response
            if (updatedCampagne.getAnnonceur() != null) {
                updatedCampagne.setAnnonceurId(updatedCampagne.getAnnonceur().getId());
            }
            
            return ResponseEntity.ok(updatedCampagne);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error processing the request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
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

    @PostMapping("/{id}/request-participation")
    @PreAuthorize("hasRole('PANELISTE')")
    public ResponseEntity<?> requestParticipation(@PathVariable int id) {
        try {
            // Get the currently authenticated panelist
            Paneliste paneliste = getCurrentPaneliste();
            if (paneliste == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Unable to identify authenticated panelist"));
            }
            
            // Use the campaign ID from the path and panelist ID from the authenticated user
            PanelisteCampagne request = participationRequestService.requestParticipation(id, paneliste.getId());
            return ResponseEntity.ok().body(Map.of("message", "Participation request submitted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error submitting participation request: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to get the currently authenticated annonceur
     * @return The authenticated Annonceur or null if not authenticated or not an Annonceur
     */
    private Annonceur getCurrentAnnonceur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Annonceur) {
            return (Annonceur) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Helper method to get the currently authenticated paneliste
     * @return The authenticated Paneliste or null if not authenticated or not a Paneliste
     */
    private Paneliste getCurrentPaneliste() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Paneliste) {
            return (Paneliste) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Get participation status for a panelist in a campaign
     * 
     * @param campaignId The ID of the campaign
     * @param panelisteId The ID of the panelist
     * @param authentication The current authentication object
     * @return The participation status or 403/404 if not allowed or not found
     */
    @GetMapping("/{campaignId}/participation-status/{panelisteId}")
    public ResponseEntity<?> getParticipationStatus(
            @PathVariable Integer campaignId,
            @PathVariable Integer panelisteId,
            Authentication authentication) {
        
        // Get authenticated user
        Object principal = authentication.getPrincipal();
        boolean isAdminOrAnnonceur = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ANNONCEUR"));
        
        // If user is a panelist, check if they're accessing their own status
        if (!isAdminOrAnnonceur && principal instanceof Paneliste) {
            Paneliste currentPaneliste = (Paneliste) principal;
            if (currentPaneliste.getId() != panelisteId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only access your own participation status"));
            }
        } else if (!isAdminOrAnnonceur) {
            // Not admin, not annonceur, and not a panelist looking at their own status
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Insufficient permissions to access this resource"));
        }
        
        try {
            Optional<ParticipationStatusDTO> status = participationRequestService.getParticipationStatus(campaignId, panelisteId);
            return status.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Error retrieving participation status: " + e.getMessage()));
        }
    }
    
    /**
     * Add questions to an existing form
     * 
     * @param campaignId The ID of the campaign
     * @param formId The ID of the form
     * @param questions The list of questions to add
     * @return The updated form with the new questions
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    @PostMapping("/{campaignId}/form/{formId}/questions")
    public ResponseEntity<?> addQuestionsToForm(
            @PathVariable int campaignId,
            @PathVariable int formId,
            @RequestBody List<Question> questions) {
        
        try {
            // 1. Validate that the campaign exists
            Campagne campaign = campagneRepository.findById(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
            
            // 2. Validate that the form exists
            Formulaire form = formulaireService.getFormulaireById(formId)
                    .orElseThrow(() -> new ResourceNotFoundException("Form not found with id: " + formId));
            
            // 3. Validate that the form belongs to the campaign
            if (form.getCampagne() == null || form.getCampagne().getId() != campaignId) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Form does not belong to the specified campaign"
                ));
            }
            
            // 4. Validate user authorization (only campaign owner or admin can add questions)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && principal instanceof Annonceur) {
                Annonceur currentAnnonceur = (Annonceur) principal;
                if (campaign.getAnnonceur() == null || campaign.getAnnonceur().getId() != currentAnnonceur.getId()) {
                    throw new AccessDeniedException("You are not authorized to modify this campaign's form");
                }
            }
            
            // 5. Add the questions to the form
            Formulaire updatedForm = formulaireService.addQuestionsToForm(formId, questions);
            
            // 6. Return the updated form
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Questions added successfully");
            response.put("form", updatedForm);
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error adding questions: " + e.getMessage()));
        }
    }
    
    /**
     * Add questions to a campaign's form using just the campaign ID
     * This simplifies the process by automatically finding the form associated with the campaign
     * 
     * @param campaignId The ID of the campaign
     * @param questions The list of questions to add
     * @return The updated form with the new questions
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    @PostMapping("/{campaignId}/questions")
    public ResponseEntity<?> addQuestionsToCampaignForm(
            @PathVariable int campaignId,
            @RequestBody List<Question> questions) {
        
        try {
            // 1. Validate that the campaign exists
            Campagne campaign = campagneRepository.findById(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
            
            // 2. Find the form associated with the campaign
            Formulaire form = formulaireService.getFormulaireByCampagne(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("No form found for campaign with id: " + campaignId));
            
            // 3. Validate user authorization (only campaign owner or admin can add questions)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && principal instanceof Annonceur) {
                Annonceur currentAnnonceur = (Annonceur) principal;
                if (campaign.getAnnonceur() == null || campaign.getAnnonceur().getId() != currentAnnonceur.getId()) {
                    throw new AccessDeniedException("You are not authorized to modify this campaign's form");
                }
            }
            
            // 4. Add the questions to the form
            Formulaire updatedForm = formulaireService.addQuestionsToForm(form.getId(), questions);
            
            // 5. Return the updated form
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Questions added successfully to campaign form");
            response.put("form", updatedForm);
            response.put("campaignId", campaignId);
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error adding questions to campaign form: " + e.getMessage()));
        }
    }
    
    /**
     * Get all questions for a campaign's form
     * 
     * @param campaignId The ID of the campaign
     * @return The list of questions associated with the campaign's form
     */
    @GetMapping("/{campaignId}/questions")
    public ResponseEntity<?> getQuestionsForCampaignForm(@PathVariable int campaignId) {
        try {
            // 1. Validate that the campaign exists
            Campagne campaign = campagneRepository.findById(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
            
            // 2. Find the form associated with the campaign
            Formulaire form = formulaireService.getFormulaireByCampagne(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("No form found for campaign with id: " + campaignId));
            
            // 3. Get the form questions
            List<FormulaireQuestion> formQuestions = formulaireService.getQuestionsForForm(form.getId());
            
            // 4. Format the response
            List<Map<String, Object>> questionsData = formQuestions.stream()
                    .map(fq -> {
                        Map<String, Object> questionData = new HashMap<>();
                        Question question = fq.getQuestion();
                        
                        questionData.put("id", question.getId());
                        questionData.put("texte", question.getTexte());
                        questionData.put("options", question.getOptions());
                        questionData.put("formulaireQuestionId", fq.getId());
                        
                        return questionData;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("campaignId", campaignId);
            response.put("formId", form.getId());
            response.put("questions", questionsData);
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving questions: " + e.getMessage()));
        }
    }
    
    // ==================== AI INTEGRATION ENDPOINTS ====================
    
    /**
     * Generate AI description for a campaign
     */
    @PostMapping("/{campaignId}/generate-description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<?> generateCampaignDescription(
            @PathVariable int campaignId,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String additionalContext) {
        
        try {
            // Validate that the campaign exists
            Campagne campaign = campagneRepository.findById(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
            
            // Validate user authorization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && principal instanceof Annonceur) {
                Annonceur currentAnnonceur = (Annonceur) principal;
                if (campaign.getAnnonceur() == null || campaign.getAnnonceur().getId() != currentAnnonceur.getId()) {
                    throw new AccessDeniedException("You are not authorized to modify this campaign");
                }
            }
            
            // Generate and update campaign description
            Campagne updatedCampaign = campaignProductAIService.generateAndUpdateCampaignDescription(
                    campaignId, targetAudience, additionalContext);
            
            // Ensure annonceurId is set in the response
            if (updatedCampaign.getAnnonceur() != null) {
                updatedCampaign.setAnnonceurId(updatedCampaign.getAnnonceur().getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Campaign description generated successfully");
            response.put("campaign", updatedCampaign);
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error generating campaign description: " + e.getMessage()));
        }
    }
    
    /**
     * Preview AI-generated campaign description without saving
     */
    @PostMapping("/preview-description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<?> previewCampaignDescription(
            @RequestParam String campaignName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String additionalContext) {
        
        try {
            AIGenerationResponseDTO response = campaignProductAIService.previewCampaignDescription(
                    campaignName, category, targetAudience, additionalContext);
            
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
                    .body(Map.of("success", false, "message", "Error previewing campaign description: " + e.getMessage()));
        }
    }
    
    /**
     * Generate AI descriptions for both campaign and its associated product
     */
    @PostMapping("/{campaignId}/generate-complete-description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<?> generateCompleteDescription(
            @PathVariable int campaignId,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String keyFeatures,
            @RequestParam(required = false) String additionalContext) {
        
        try {
            // Validate that the campaign exists
            Campagne campaign = campagneRepository.findById(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
            
            // Validate user authorization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && principal instanceof Annonceur) {
                Annonceur currentAnnonceur = (Annonceur) principal;
                if (campaign.getAnnonceur() == null || campaign.getAnnonceur().getId() != currentAnnonceur.getId()) {
                    throw new AccessDeniedException("You are not authorized to modify this campaign");
                }
            }
            
            // Generate descriptions for both campaign and product
            Campagne updatedCampaign = campaignProductAIService.generateCampaignAndProductDescriptions(
                    campaignId, targetAudience, keyFeatures, additionalContext);
            
            // Ensure annonceurId is set in the response
            if (updatedCampaign.getAnnonceur() != null) {
                updatedCampaign.setAnnonceurId(updatedCampaign.getAnnonceur().getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Campaign and product descriptions generated successfully");
            response.put("campaign", updatedCampaign);
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error generating complete description: " + e.getMessage()));
        }
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
    // The image URL will be stored in the campagne.imageUrl field
}