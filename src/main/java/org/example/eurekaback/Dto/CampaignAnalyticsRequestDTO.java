package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampaignAnalyticsRequestDTO {
    
    private Integer campaignId;
    private boolean includeKeywords = true;
    private boolean includeDailyTrends = true;
    private boolean forceRefresh = false;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxKeywords = 20;
}