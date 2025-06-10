package org.example.eurekaback.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank
    private String nom;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String motDePasse;

    @NotBlank
    private String userType; // "ADMIN", "ANNONCEUR" or "PANELISTE"
    
    // Fields specific to Annonceur
    private String entreprise;
    
    // Fields specific to Paneliste
    private List<String> interets = new ArrayList<>();
}