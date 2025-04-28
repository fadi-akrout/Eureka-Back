package org.example.eurekaback.Controller;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Admin;
import org.example.eurekaback.Service.ServiceAdmin.AdminService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
    public ResponseEntity<Admin> createAdmin( @Valid @RequestBody Admin admin) {
        return new ResponseEntity<>(adminService.createAdmin(admin), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable int id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}