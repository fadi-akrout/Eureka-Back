package org.example.eurekaback.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.ParticipationResult;
import org.example.eurekaback.Service.GamificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class GamificationEventListener {

    private final GamificationService gamificationService;

    @EventListener
    @Async
    public CompletableFuture<Void> handleCampaignCompleted(CampaignCompletedEvent event) {
        try {
            log.info("Processing gamification update for user {} and campaign {}",
                event.getUserId(), event.getCampaignId());

            ParticipationResult result = gamificationService.recordParticipation(
                event.getUserId(),
                event.getCampaignId()
            );

            // Log achievements
            if (result.getLevelChanged()) {
                log.info("User {} leveled up to {}", event.getUserId(), result.getNewLevel());
            }

            if (!result.getNewlyUnlockedBadges().isEmpty()) {
                log.info("User {} unlocked {} new badges",
                    event.getUserId(), result.getNewlyUnlockedBadges().size());
            }

            if (!result.getNewlyUnlockedRewards().isEmpty()) {
                log.info("User {} unlocked {} new rewards",
                    event.getUserId(), result.getNewlyUnlockedRewards().size());
            }

            log.info("Gamification update completed successfully for user {}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to update gamification for user {} and campaign {}",
                event.getUserId(), event.getCampaignId(), e);
        }

        return CompletableFuture.completedFuture(null);
    }
}
