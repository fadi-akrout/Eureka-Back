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
public class RewardClaimResult {
    private Boolean success;
    private RewardDTO reward;
    private String message;
}
