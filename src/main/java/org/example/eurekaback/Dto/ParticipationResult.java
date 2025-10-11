package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eurekaback.Entity.GamificationLevel;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationResult {
    private Boolean success;
    private Integer newParticipationCount;
    private Boolean levelChanged;
    private GamificationLevel oldLevel;
    private GamificationLevel newLevel;
    private List<BadgeDTO> newlyUnlockedBadges;
    private List<RewardDTO> newlyUnlockedRewards;
    private String message;
}
