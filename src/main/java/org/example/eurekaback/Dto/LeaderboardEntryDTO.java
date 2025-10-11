package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntryDTO {
    private Long userId;
    private String userEmail;
    private String userName;
    private Integer points;
    private Integer eventsCount;
    private Integer rank;
}
