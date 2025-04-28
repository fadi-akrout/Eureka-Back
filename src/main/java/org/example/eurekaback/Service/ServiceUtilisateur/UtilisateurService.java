package org.example.eurekaback.Service.ServiceUtilisateur;



import org.example.eurekaback.Entity.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface UtilisateurService {
    List<Utilisateur> getAllUtilisateurs();
    Optional<Utilisateur> getUtilisateurById(int id);
    Optional<Utilisateur> getUtilisateurByEmail(String email);
    Utilisateur createUtilisateur(Utilisateur utilisateur);
    void deleteUtilisateur(int id);
}
