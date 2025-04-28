package org.example.eurekaback.Service.ServiceReponse;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Reponse;
import org.example.eurekaback.Repository.ReponseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReponseServiceImpl implements ReponseService {
    private final ReponseRepository reponseRepository;

    @Override
    public List<Reponse> getAllReponses() {
        return reponseRepository.findAll();
    }

    @Override
    public Optional<Reponse> getReponseById(int id) {
        return reponseRepository.findById(id);
    }

    @Override
    public Reponse createReponse(Reponse reponse) {
        return reponseRepository.save(reponse);
    }

    @Override
    public void deleteReponse(int id) {
        reponseRepository.deleteById(id);
    }
}
