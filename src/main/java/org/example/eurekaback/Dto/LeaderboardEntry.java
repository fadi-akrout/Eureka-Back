package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eurekaback.Entity.GamificationLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private Long userId;
    private String username;
    private Integer participationsCount;
    private GamificationLevel level;
    private String avatarUrl;
    private Integer rank;
}
