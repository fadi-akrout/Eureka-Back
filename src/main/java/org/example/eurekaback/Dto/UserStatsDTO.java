package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsDTO {
    private long totalUsers;
    private long totalAdmins;
    private long totalAnnonceurs;
    private long totalPanelistes;
    private List<UserDTO> recentRegistrations;
}