package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationHistoryDTO {
    private Long panelisteId;
    private Integer totalParticipations;
    private List<ParticipationDTO> participations;
}