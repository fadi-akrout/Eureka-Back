package org.example.eurekaback.Service.ServiceAnnonceur;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Annonceur;
import org.example.eurekaback.Repository.AnnonceurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AnnonceurServiceImpl implements AnnonceurService {
    private final AnnonceurRepository annonceurRepository;

    @Override
    public List<Annonceur> getAllAnnonceurs() {
        return annonceurRepository.findAll();
    }

    @Override
    public Optional<Annonceur> getAnnonceurById(int id) {
        return annonceurRepository.findById(id);
    }

    @Override
    public Annonceur createAnnonceur(Annonceur annonceur) {
        return annonceurRepository.save(annonceur);
    }

    @Override
    public void deleteAnnonceur(int id) {
        annonceurRepository.deleteById(id);
    }
}
