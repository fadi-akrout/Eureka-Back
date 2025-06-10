package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithCampaignsDTO {
    private Long id;
    private String nom;
    private String email;
    private String userType;
    private int campaignsParticipated;
    private int campaignsCreated;
    private Date lastLogin; // For future implementation
    private Date dateInscription;
}