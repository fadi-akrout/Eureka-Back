package org.example.eurekaback.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.MessageResponse;
import org.example.eurekaback.Service.ParticipationRequestMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {
    
    private final ParticipationRequestMigrationService migrationService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
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
     * Run the Reponse schema update migration
     */
    @PostMapping("/reponse-schema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> migrateReponseSchema() {
        log.info("Reponse schema migration endpoint called");
        try {
            log.info("Starting Reponse schema migration...");
            
            // Load SQL from file
            InputStream is = getClass().getResourceAsStream("/db/migration/update_reponse_schema.sql");
            if (is == null) {
                throw new RuntimeException("Could not find migration SQL file");
            }
            
            String sql = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
            
            // Execute SQL statements
            String[] statements = sql.split(";");
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    log.info("Executing SQL: {}", statement);
                    jdbcTemplate.execute(statement);
                }
            }
            
            log.info("Reponse schema migration completed successfully");
            return ResponseEntity.ok(new MessageResponse("Reponse schema migration completed successfully"));
        } catch (Exception e) {
            log.error("Reponse schema migration failed: ", e);
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Reponse schema migration failed: " + e.getMessage()));
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