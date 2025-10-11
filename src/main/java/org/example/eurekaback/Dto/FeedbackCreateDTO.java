package org.example.eurekaback.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackCreateDTO {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer note;
    
    @NotBlank(message = "Comment is required")
    private String commentaire;
    
    @NotNull(message = "Campaign ID is required")
    private Long campagneId;
    
    @NotNull(message = "Panelist ID is required")
    private Long panelisteId;
}