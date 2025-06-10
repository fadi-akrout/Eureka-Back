package org.example.eurekaback.Controller;

import jakarta.validation.Valid;
import org.example.eurekaback.Dto.JwtResponse;
import org.example.eurekaback.Dto.LoginRequest;
import org.example.eurekaback.Dto.MessageResponse;
import org.example.eurekaback.Dto.SignupRequest;
import org.example.eurekaback.Entity.Admin;
import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.AdminRepository;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Repository.UtilisateurRepository;
import org.example.eurekaback.Security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private AnnonceurRepository annonceurRepository;
    
    @Autowired
    private PanelisteRepository panelisteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getMotDePasse()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        // Determine user type based on class
        String userType;
        if (utilisateur instanceof Admin) {
            userType = "ADMIN";
        } else if (utilisateur instanceof Annonceur) {
            userType = "ANNONCEUR";
        } else if (utilisateur instanceof Paneliste) {
            userType = "PANELISTE";
        } else {
            userType = utilisateur.getRole(); // Fallback to role for backward compatibility
        }

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                userType));
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(new MessageResponse("Auth endpoint is working!"));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        System.out.println("Registration attempt for email: " + signupRequest.getEmail());
        
        try {
            if (utilisateurRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
                System.out.println("Email already in use: " + signupRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Determine user type and register accordingly
            String userType = signupRequest.getUserType().toUpperCase();
            
            switch (userType) {
                case "ADMIN":
                    return registerAdmin(signupRequest);
                case "ANNONCEUR":
                    return registerAnnonceur(signupRequest);
                case "PANELISTE":
                    return registerPaneliste(signupRequest);
                default:
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Error: Invalid user type. Must be ADMIN, ANNONCEUR or PANELISTE"));
            }
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Error during registration: " + e.getMessage()));
        }
    }
    
    private ResponseEntity<?> registerAnnonceur(SignupRequest signupRequest) {
        try {
            // Create new Annonceur
            Annonceur annonceur = new Annonceur();
            annonceur.setNom(signupRequest.getNom());
            annonceur.setEmail(signupRequest.getEmail());
            annonceur.setMotDePasse(passwordEncoder.encode(signupRequest.getMotDePasse()));
            annonceur.setRole("ANNONCEUR");
            annonceur.setDateInscription(new Date());
            annonceur.setEntreprise(signupRequest.getEntreprise());
            
            annonceurRepository.save(annonceur);
            System.out.println("Annonceur registered successfully: " + signupRequest.getEmail());
            
            return ResponseEntity.ok(new MessageResponse("Annonceur registered successfully!"));
        } catch (Exception e) {
            System.err.println("Error during Annonceur registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Error during Annonceur registration: " + e.getMessage()));
        }
    }
    
    private ResponseEntity<?> registerPaneliste(SignupRequest signupRequest) {
        try {
            // Create new Paneliste
            Paneliste paneliste = new Paneliste();
            paneliste.setNom(signupRequest.getNom());
            paneliste.setEmail(signupRequest.getEmail());
            paneliste.setMotDePasse(passwordEncoder.encode(signupRequest.getMotDePasse()));
            paneliste.setRole("PANELISTE");
            paneliste.setDateInscription(new Date());
            paneliste.setInterets(signupRequest.getInterets());
            
            panelisteRepository.save(paneliste);
            System.out.println("Paneliste registered successfully: " + signupRequest.getEmail());
            
            return ResponseEntity.ok(new MessageResponse("Paneliste registered successfully!"));
        } catch (Exception e) {
            System.err.println("Error during Paneliste registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Error during Paneliste registration: " + e.getMessage()));
        }
    }
    
    private ResponseEntity<?> registerAdmin(SignupRequest signupRequest) {
        try {
            // Create new Admin
            Admin admin = new Admin();
            admin.setNom(signupRequest.getNom());
            admin.setEmail(signupRequest.getEmail());
            admin.setMotDePasse(passwordEncoder.encode(signupRequest.getMotDePasse()));
            admin.setRole("ADMIN");
            admin.setDateInscription(new Date());
            
            // Set default admin permissions
            admin.getPermissions().add("MANAGE_USERS");
            admin.getPermissions().add("MANAGE_CAMPAIGNS");
            admin.getPermissions().add("MANAGE_PANELISTS");
            admin.getPermissions().add("MANAGE_SYSTEM");
            
            adminRepository.save(admin);
            System.out.println("Admin registered successfully: " + signupRequest.getEmail());
            
            return ResponseEntity.ok(new MessageResponse("Admin registered successfully!"));
        } catch (Exception e) {
            System.err.println("Error during Admin registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Error during Admin registration: " + e.getMessage()));
        }
    }
}