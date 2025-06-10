package org.example.eurekaback.Dto;

import lombok.Data;

@Data
public class ParticipationApprovalRequest {
    private Integer campagneId;
    private Integer panelisteId;
    private Integer annonceurId;
    private String feedback; // For rejection feedback
}