package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampaignAnalyticsResponseDTO {
    
    private boolean success;
    private String message;
    private CampaignAnalyticsDTO data;
    private String errorCode;
    
    public CampaignAnalyticsResponseDTO(String errorMessage) {
        this.success = false;
        this.message = errorMessage;
    }
    
    public CampaignAnalyticsResponseDTO(CampaignAnalyticsDTO data) {
        this.success = true;
        this.message = "Analytics generated successfully";
        this.data = data;
    }
}