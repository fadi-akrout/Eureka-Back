package org.example.eurekaback.Service.ServiceCampagne;

import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.Produit;

import java.util.List;
import java.util.Optional;

public interface CampagneService {
    List<Campagne> getAllCampagnes();
    Optional<Campagne> getCampagneById(int id);
    Campagne createCampagne(Campagne campagne);
    void deleteCampagne(int id);

    Campagne createCampagneWithProduitAndFormulaire(Campagne campagne, Produit produit, Formulaire formulaire);
    List<Campagne> getCampagnesActives();
    void assignerPanelistesACampagne(Campagne campagne, List<Paneliste> panelistes);
    void demarrerCampagne(int campagneId);
    void terminerCampagne(int campagneId);

}
