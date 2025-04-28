package org.example.eurekaback.Service.ServiceAnnonceur;

import org.example.eurekaback.Entity.Annonceur;

import java.util.List;
import java.util.Optional;

public interface AnnonceurService {
    List<Annonceur> getAllAnnonceurs();
    Optional<Annonceur> getAnnonceurById(int id);
    Annonceur createAnnonceur(Annonceur annonceur);
    void deleteAnnonceur(int id);
}
