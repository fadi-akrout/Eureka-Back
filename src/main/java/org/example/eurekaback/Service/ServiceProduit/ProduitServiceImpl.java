package org.example.eurekaback.Service.ServiceProduit;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Produit;
import org.example.eurekaback.Repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProduitServiceImpl implements ProduitService {
    private final ProduitRepository produitRepository;

    @Override
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    @Override
    public Optional<Produit> getProduitById(int id) {
        return produitRepository.findById(id);
    }

    @Override
    public Produit createProduit(Produit produit) {
        return produitRepository.save(produit);
    }

    @Override
    public void deleteProduit(int id) {
        produitRepository.deleteById(id);
    }
}
