package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eurekaback.Entity.PanelisteCampagne;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationStatusDTO {
    private Integer id;
    private Integer panelisteId;
    private Integer campagneId;
    private PanelisteCampagne.StatutParticipation statut;
    private LocalDateTime dateRequest;
    private LocalDateTime dateInvitation;
    private LocalDateTime dateReponse;
    private LocalDateTime dateApproval;
    private LocalDateTime dateCompletion;
    private String feedback;
    private boolean canRequest;
    private boolean canCancel;
}