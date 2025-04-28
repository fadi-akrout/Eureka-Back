package org.example.eurekaback.Service.ServiceReponse;

import org.example.eurekaback.Entity.Reponse;

import java.util.List;
import java.util.Optional;

public interface ReponseService {
    List<Reponse> getAllReponses();
    Optional<Reponse> getReponseById(int id);
    Reponse createReponse(Reponse reponse);
    void deleteReponse(int id);
}
