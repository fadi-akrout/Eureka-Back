package org.example.eurekaback.Service.ServiceParticipationRequest;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.eurekaback.Dto.ParticipationRequestDTO;
import org.example.eurekaback.Dto.ParticipationStatusDTO;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.PanelisteCampagne;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Repository.PanelisteCampagneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    
    private final PanelisteCampagneRepository panelisteCampagneRepository;
    private final CampagneRepository campagneRepository;
    private final PanelisteRepository panelisteRepository;
    
    @Override
    public PanelisteCampagne requestParticipation(Integer campagneId, Integer panelisteId) {
        // Validate campaign exists and is active
        Campagne campagne = campagneRepository.findById(campagneId)
            .orElseThrow(() -> new EntityNotFoundException("Campaign not found"));
            
        if (!"ACTIVE".equals(campagne.getStatut())) {
            throw new IllegalStateException("Campaign is not active");
        }
        
        // Validate panelist exists
        Paneliste paneliste = panelisteRepository.findById(panelisteId)
            .orElseThrow(() -> new EntityNotFoundException("Panelist not found"));
        
        // Check if already has a relationship
        Optional<PanelisteCampagne> existing = panelisteCampagneRepository
            .findByCampagneIdAndPanelisteId(campagneId, panelisteId);
            
        if (existing.isPresent()) {
            PanelisteCampagne pc = existing.get();
            if (pc.getStatut() == PanelisteCampagne.StatutParticipation.PENDING) {
                throw new IllegalStateException("Participation request already pending");
            } else if (pc.getStatut() == PanelisteCampagne.StatutParticipation.ACCEPTE || 
                      pc.getStatut() == PanelisteCampagne.StatutParticipation.EN_COURS ||
                      pc.getStatut() == PanelisteCampagne.StatutParticipation.COMPLETE) {
                throw new IllegalStateException("Already participating in campaign");
            }
            // If REFUSE, allow to request again by updating the record
            pc.setStatut(PanelisteCampagne.StatutParticipation.PENDING);
            pc.setDateRequest(LocalDateTime.now());
            pc.setDateApproval(null);
            pc.setFeedback(null);
            return panelisteCampagneRepository.save(pc);
        }
        
        // Create new participation request
        PanelisteCampagne participationRequest = new PanelisteCampagne();
        participationRequest.setCampagne(campagne);
        participationRequest.setPaneliste(paneliste);
        participationRequest.setStatut(PanelisteCampagne.StatutParticipation.PENDING);
        participationRequest.setDateRequest(LocalDateTime.now());
        
        return panelisteCampagneRepository.save(participationRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDTO> getPendingRequests(Integer campagneId) {
        List<PanelisteCampagne> requests = panelisteCampagneRepository
            .findPendingRequestsWithPanelistDetails(campagneId, PanelisteCampagne.StatutParticipation.PENDING);
            
        return requests.stream()
            .map(this::convertToRequestDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public PanelisteCampagne approveRequest(Integer campagneId, Integer panelisteId, Integer annonceurId) {
        PanelisteCampagne request = panelisteCampagneRepository
            .findByCampagneIdAndPanelisteId(campagneId, panelisteId)
            .orElseThrow(() -> new EntityNotFoundException("Participation request not found"));
            
        if (request.getStatut() != PanelisteCampagne.StatutParticipation.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        
        // Verify annonceur is the campaign owner
        Campagne campagne = request.getCampagne();
        if (campagne.getAnnonceur().getId() != annonceurId) {
            throw new SecurityException("Not authorized to approve this request");
        }
        
        request.setStatut(PanelisteCampagne.StatutParticipation.ACCEPTE);
        request.setDateApproval(LocalDateTime.now());
        request.setDateReponse(LocalDateTime.now());
        
        return panelisteCampagneRepository.save(request);
    }
    
    @Override
    public PanelisteCampagne rejectRequest(Integer campagneId, Integer panelisteId, Integer annonceurId, String feedback) {
        PanelisteCampagne request = panelisteCampagneRepository
            .findByCampagneIdAndPanelisteId(campagneId, panelisteId)
            .orElseThrow(() -> new EntityNotFoundException("Participation request not found"));
            
        if (request.getStatut() != PanelisteCampagne.StatutParticipation.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        
        // Verify annonceur is the campaign owner
        Campagne campagne = request.getCampagne();
        if (campagne.getAnnonceur().getId() != annonceurId) {
            throw new SecurityException("Not authorized to reject this request");
        }
        
        request.setStatut(PanelisteCampagne.StatutParticipation.REFUSE);
        request.setDateApproval(LocalDateTime.now());
        request.setFeedback(feedback);
        
        return panelisteCampagneRepository.save(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ParticipationStatusDTO> getParticipationStatus(Integer campagneId, Integer panelisteId) {
        return panelisteCampagneRepository
            .findByCampagneIdAndPanelisteId(campagneId, panelisteId)
            .map(this::convertToStatusDTO);
    }
    
    @Override
    public void cancelRequest(Integer campagneId, Integer panelisteId) {
        PanelisteCampagne request = panelisteCampagneRepository
            .findByCampagneIdAndPanelisteId(campagneId, panelisteId)
            .orElseThrow(() -> new EntityNotFoundException("Participation request not found"));
            
        if (request.getStatut() != PanelisteCampagne.StatutParticipation.PENDING) {
            throw new IllegalStateException("Can only cancel pending requests");
        }
        
        panelisteCampagneRepository.delete(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDTO> getRequestsByPanelist(Integer panelisteId) {
        List<PanelisteCampagne> requests = panelisteCampagneRepository
            .findByPanelisteIdAndStatut(panelisteId, PanelisteCampagne.StatutParticipation.PENDING);
            
        return requests.stream()
            .map(this::convertToRequestDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countPendingRequests(Integer campagneId) {
        return panelisteCampagneRepository.countByCampagneIdAndStatut(campagneId, PanelisteCampagne.StatutParticipation.PENDING);
    }
    
    private ParticipationRequestDTO convertToRequestDTO(PanelisteCampagne pc) {
        ParticipationRequestDTO dto = new ParticipationRequestDTO();
        dto.setId(pc.getId());
        dto.setPanelisteId(pc.getPaneliste().getId());
        dto.setPanelisteNom(pc.getPaneliste().getNom());
        dto.setPanelisteEmail(pc.getPaneliste().getEmail());
        dto.setCampagneId(pc.getCampagne().getId());
        dto.setCampagneNom(pc.getCampagne().getNom());
        dto.setStatut(pc.getStatut());
        dto.setDateRequest(pc.getDateRequest());
        dto.setDateApproval(pc.getDateApproval());
        dto.setFeedback(pc.getFeedback());
        return dto;
    }
    
    private ParticipationStatusDTO convertToStatusDTO(PanelisteCampagne pc) {
        ParticipationStatusDTO dto = new ParticipationStatusDTO();
        dto.setId(pc.getId());
        dto.setPanelisteId(pc.getPaneliste().getId());
        dto.setCampagneId(pc.getCampagne().getId());
        dto.setStatut(pc.getStatut());
        dto.setDateRequest(pc.getDateRequest());
        dto.setDateInvitation(pc.getDateInvitation());
        dto.setDateReponse(pc.getDateReponse());
        dto.setDateApproval(pc.getDateApproval());
        dto.setDateCompletion(pc.getDateCompletion());
        dto.setFeedback(pc.getFeedback());
        
        // Set flags based on current status
        dto.setCanRequest(pc.getStatut() == PanelisteCampagne.StatutParticipation.REFUSE || 
                         pc.getStatut() == null);
        dto.setCanCancel(pc.getStatut() == PanelisteCampagne.StatutParticipation.PENDING);
        
        return dto;
    }
}