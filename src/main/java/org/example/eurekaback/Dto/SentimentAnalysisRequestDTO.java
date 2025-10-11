package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SentimentAnalysisRequestDTO {
    
    @NotBlank(message = "Text is required for sentiment analysis")
    @Size(max = 5000, message = "Text must not exceed 5000 characters")
    private String text;
    
    // Optional: Campaign ID for tracking purposes
    private Long campaignId;
    
    // Optional: Additional context
    private String context;
}