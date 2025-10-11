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
public class GamificationProfileDTO {
    private Long userId;
    private Integer participationsCount;
    private GamificationLevel level;
    private Double currentLevelProgress;
    private Integer nextLevelThreshold;
    private Integer participationsToNextLevel;
    private List<BadgeDTO> badges;
    private List<RewardDTO> rewards;
}
