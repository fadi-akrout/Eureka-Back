package org.example.eurekaback.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.MessageResponse;
import org.example.eurekaback.Service.ParticipationRequestMigrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class MigrationController {
    
    private final ParticipationRequestMigrationService migrationService;
    
    /**
     * Migrate existing PanelisteCampagne records for participation request system
     */
    @PostMapping("/participation-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> migrateParticipationRequests() {
        log.info("Migration endpoint called");
        try {
            log.info("Starting migration process...");
            migrationService.migrateExistingRecords();
            log.info("Migration completed successfully");
            return ResponseEntity.ok(new MessageResponse("Migration completed successfully"));
        } catch (Exception e) {
            log.error("Migration failed: ", e);
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Migration failed: " + e.getMessage()));
        }
    }
    
    /**
     * Validate migration results
     */
    @GetMapping("/participation-requests/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> validateMigration() {
        log.info("Validation endpoint called");
        try {
            log.info("Starting validation process...");
            migrationService.validateMigration();
            log.info("Validation completed successfully");
            return ResponseEntity.ok(new MessageResponse("Migration validation completed"));
        } catch (Exception e) {
            log.error("Validation failed: ", e);
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Validation failed: " + e.getMessage()));
        }
    }
    
    /**
     * Simple test endpoint to check if migration endpoints are accessible
     */
    @GetMapping("/test")
    public ResponseEntity<MessageResponse> testEndpoint() {
        log.info("Test endpoint called");
        return ResponseEntity.ok(new MessageResponse("Migration endpoints are accessible!"));
    }
}