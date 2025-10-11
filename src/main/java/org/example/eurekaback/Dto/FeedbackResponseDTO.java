package org.example.eurekaback.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponseDTO {
    private Long id;
    private Integer note;
    private String commentaire;
    private LocalDateTime date;
    private Long panelisteId;
    private String panelisteName;
    private String panelisteEmail;
    private Long campagneId;
    private String campagneName;
    private String campagneDescription;
}