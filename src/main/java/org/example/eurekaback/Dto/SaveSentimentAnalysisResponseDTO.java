package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveSentimentAnalysisResponseDTO {
    
    private boolean success;
    private String message;
    private Integer campaignId;
    private Integer resultatAnalyseId;
    private LocalDateTime savedAt;
    private String errorMessage;
    
    // Constructor for successful response
    public SaveSentimentAnalysisResponseDTO(boolean success, String message, Integer campaignId, Integer resultatAnalyseId, LocalDateTime savedAt) {
        this.success = success;
        this.message = message;
        this.campaignId = campaignId;
        this.resultatAnalyseId = resultatAnalyseId;
        this.savedAt = savedAt;
    }
    
    // Constructor for error response
    public SaveSentimentAnalysisResponseDTO(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
}