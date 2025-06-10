package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private int id;
    private String nom;
    private String email;
    private String role;
    private String userType;

    public JwtResponse(String token, int id, String nom, String email, String role) {
        this.token = token;
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.userType = role; // For backward compatibility
    }
    
    public JwtResponse(String token, int id, String nom, String email, String role, String userType) {
        this.token = token;
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.userType = userType;
    }
}