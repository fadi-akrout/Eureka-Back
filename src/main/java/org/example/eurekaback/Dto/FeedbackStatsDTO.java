package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackStatsDTO {
    private Long totalFeedbacks;
    private Double averageRating;
    private Map<Integer, Long> ratingDistribution; // rating -> count
}