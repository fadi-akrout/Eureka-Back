package org.example.eurekaback.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DataMigrationService {

    @Autowired
    private PanelisteCampagneRepository panelisteCampagneRepository;

    /**
     * Automatically runs on application startup to fix NULL statut values
     */
    @PostConstruct
    @Transactional
    public void initializeMigration() {
        try {
            log.info("Starting automatic data migration for NULL statut values...");
            migrateExistingPanelisteCampagneData();
            log.info("Data migration completed successfully.");
        } catch (Exception e) {
            log.error("Error during automatic data migration: ", e);
            // Don't throw exception to prevent application startup failure
        }
    }

    /**
     * Migrates existing PanelisteCampagne records to set default status
     * This should be called once after deploying the new status field
     */
    @Transactional
    public void migrateExistingPanelisteCampagneData() {
        List<PanelisteCampagne> allRecords = panelisteCampagneRepository.findAll();
        int updatedCount = 0;
        
        for (PanelisteCampagne pc : allRecords) {
            // If status is null (existing data), set it to ACCEPTE
            // This assumes existing records represent actual participations
            if (pc.getStatut() == null) {
                pc.setStatut(PanelisteCampagne.StatutParticipation.ACCEPTE);
                pc.setDateInvitation(LocalDateTime.now().minusDays(1)); // Set a default past date
                pc.setDateReponse(LocalDateTime.now().minusDays(1));
                panelisteCampagneRepository.save(pc);
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            log.info("Updated {} records with NULL statut to ACCEPTE status", updatedCount);
        } else {
            log.info("No records with NULL statut found. Migration not needed.");
        }
    }

    /**
     * Sets all existing records to COMPLETE status
     * Use this if you want to mark all existing participations as completed
     */
    @Transactional
    public void markExistingAsCompleted() {
        List<PanelisteCampagne> allRecords = panelisteCampagneRepository.findAll();
        
        for (PanelisteCampagne pc : allRecords) {
            if (pc.getStatut() == null || pc.getStatut() == PanelisteCampagne.StatutParticipation.ACCEPTE) {
                pc.setStatut(PanelisteCampagne.StatutParticipation.COMPLETE);
                pc.setDateCompletion(LocalDateTime.now().minusDays(1));
                panelisteCampagneRepository.save(pc);
            }
        }
    }
}