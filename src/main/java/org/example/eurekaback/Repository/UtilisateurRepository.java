package org.example.eurekaback.Repository;
import org.example.eurekaback.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByDateInscriptionAfterOrderByDateInscriptionDesc(Date date);
    long countByRole(String role);
}
