package org.example.eurekaback.Service.ServiceProduit;

import org.example.eurekaback.Entity.Produit;

import java.util.List;
import java.util.Optional;

public interface ProduitService {
    List<Produit> getAllProduits();
    Optional<Produit> getProduitById(int id);
    Produit createProduit(Produit produit);
    void deleteProduit(int id);
}