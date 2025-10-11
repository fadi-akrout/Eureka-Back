package org.example.eurekaback.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignCompletedEvent {
    private Long userId;
    private Long campaignId;
    private String userEmail;
}
