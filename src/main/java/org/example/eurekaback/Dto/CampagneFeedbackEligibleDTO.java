package org.example.eurekaback.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampagneFeedbackEligibleDTO {
    private Long campagneId;
    private String campagneName;
    private String campagneDescription;
    private Date dateDebut;
    private Date dateFin;
    private String statut;
    private boolean hasSubmittedForm;
    private boolean hasFeedback;
    private String imageUrl;
}