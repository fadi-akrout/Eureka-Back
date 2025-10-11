package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eurekaback.Entity.PanelisteCampagne;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationDTO {
    private Long campaignId;
    private String campaignName;
    private String campaignStatus;
    private Date campaignStartDate;
    private Date campaignEndDate;
    private PanelisteCampagne.StatutParticipation participationStatus;
    private LocalDateTime invitationDate;
    private LocalDateTime responseDate;
}