package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDTO {
    private String id;
    private String name;
    private String description;
    private Integer requiredParticipations;
    private String iconClass;
    private Boolean isUnlocked;
    private LocalDateTime unlockedAt;
}
