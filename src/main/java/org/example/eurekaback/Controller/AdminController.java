package org.example.eurekaback.Controller;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Admin;
import org.example.eurekaback.Entity.Utilisateur;
import org.example.eurekaback.Repository.UtilisateurRepository;
import org.example.eurekaback.Service.ServiceAdmin.AdminService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable int id) {
        return adminService.getAdminById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Admin> createAdmin(@Valid @RequestBody Admin admin) {
        return new ResponseEntity<>(adminService.createAdmin(admin), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable int id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> partialUpdateAdmin(@PathVariable int id, @RequestBody Admin updatedAdmin) {
        try {
            // First check if the user exists in the base table
            Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
            // Check if this user is already an admin, if not, create the record
            Admin existingAdmin;
            try {
                existingAdmin = adminService.getAdminById(id)
                    .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
            } catch (RuntimeException e) {
                // User exists but is not an admin yet - check if they should be
                if ("ADMIN".equals(utilisateur.getRole())) {
                    // Create a new admin record
                    existingAdmin = new Admin();
                    existingAdmin.setId(id);
                    // Set other required fields
                } else {
                    // User is not an admin by role
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "User exists but does not have ADMIN role. Update role first."));
                }
            }
            
            // Update admin-specific fields if needed
            
            // Save the updated admin
            return ResponseEntity.ok(adminService.createAdmin(existingAdmin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
        }
    }
}