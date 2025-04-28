package org.example.eurekaback.Service.ServiceAdmin;

import org.example.eurekaback.Entity.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    List<Admin> getAllAdmins();
    Optional<Admin> getAdminById(int id);
    Admin createAdmin(Admin admin);
    void deleteAdmin(int id);
}
