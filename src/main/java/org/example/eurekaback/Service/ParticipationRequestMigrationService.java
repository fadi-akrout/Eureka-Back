package org.example.eurekaback.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestMigrationService {
    
    private final PanelisteCampagneRepository panelisteCampagneRepository;
    
    /**
     * Migrate existing PanelisteCampagne records to support the new participation request system
     */
    @Transactional
    public void migrateExistingRecords() {
        log.info("Starting migration of existing PanelisteCampagne records...");
        
        List<PanelisteCampagne> allRecords = panelisteCampagneRepository.findAll();
        int updatedCount = 0;
        
        for (PanelisteCampagne pc : allRecords) {
            boolean updated = false;
            
            // Set dateRequest for existing records if not already set
            if (pc.getDateRequest() == null) {
                if (pc.getDateInvitation() != null) {
                    pc.setDateRequest(pc.getDateInvitation());
                } else if (pc.getDateReponse() != null) {
                    pc.setDateRequest(pc.getDateReponse());
                } else {
                    pc.setDateRequest(LocalDateTime.now());
                }
                updated = true;
            }
            
            // Set dateApproval for accepted/refused records if not already set
            if (pc.getDateApproval() == null && 
                (pc.getStatut() == PanelisteCampagne.StatutParticipation.ACCEPTE || 
                 pc.getStatut() == PanelisteCampagne.StatutParticipation.REFUSE)) {
                if (pc.getDateReponse() != null) {
                    pc.setDateApproval(pc.getDateReponse());
                } else {
                    pc.setDateApproval(LocalDateTime.now());
                }
                updated = true;
            }
            
            if (updated) {
                panelisteCampagneRepository.save(pc);
                updatedCount++;
            }
        }
        
        log.info("Migration completed. Updated {} records out of {} total records.", updatedCount, allRecords.size());
    }
    
    /**
     * Validate the migration results
     */
    @Transactional(readOnly = true)
    public void validateMigration() {
        log.info("Validating migration results...");
        
        List<PanelisteCampagne> allRecords = panelisteCampagneRepository.findAll();
        int invalidRecords = 0;
        
        for (PanelisteCampagne pc : allRecords) {
            if (pc.getDateRequest() == null) {
                log.warn("Record {} has null dateRequest", pc.getId());
                invalidRecords++;
            }
            
            if ((pc.getStatut() == PanelisteCampagne.StatutParticipation.ACCEPTE || 
                 pc.getStatut() == PanelisteCampagne.StatutParticipation.REFUSE) && 
                pc.getDateApproval() == null) {
                log.warn("Record {} has status {} but null dateApproval", pc.getId(), pc.getStatut());
                invalidRecords++;
            }
        }
        
        if (invalidRecords == 0) {
            log.info("Migration validation successful. All records are valid.");
        } else {
            log.warn("Migration validation found {} invalid records.", invalidRecords);
        }
    }
}