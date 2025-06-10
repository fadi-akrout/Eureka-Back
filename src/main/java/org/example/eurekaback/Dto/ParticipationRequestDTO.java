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
public class ParticipationRequestDTO {
    private Integer id;
    private Integer panelisteId;
    private String panelisteNom;
    private String panelisteEmail;
    private Integer campagneId;
    private String campagneNom;
    private PanelisteCampagne.StatutParticipation statut;
    private LocalDateTime dateRequest;
    private LocalDateTime dateApproval;
    private String feedback;
}