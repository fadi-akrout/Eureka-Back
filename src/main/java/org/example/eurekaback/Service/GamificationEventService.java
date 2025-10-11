package org.example.eurekaback.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Event.CampaignCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationEventService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Trigger gamification update when a campaign is completed
     */
    public void triggerCampaignCompletion(Long userId, Long campaignId, String userEmail) {
        log.info("Triggering campaign completion event for user {} and campaign {}", userId, campaignId);

        CampaignCompletedEvent event = new CampaignCompletedEvent(userId, campaignId, userEmail);
        eventPublisher.publishEvent(event);
    }

    /**
     * Trigger gamification update when a campaign is completed (simplified version)
     */
    public void triggerCampaignCompletion(Long userId, Long campaignId) {
        triggerCampaignCompletion(userId, campaignId, null);
    }
}
