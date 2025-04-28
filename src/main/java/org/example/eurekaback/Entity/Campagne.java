package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Campagne")
public class Campagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nom;
    private String description;
    private Date dateDebut;
    private Date dateFin;
    private String statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonceur_id")
    private Annonceur annonceur;

    @OneToOne(mappedBy = "campagne", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)

    private Produit produit;

    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToOne(mappedBy = "campagne", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Formulaire formulaire;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "resultat_analyse_id", unique = true, nullable = true)
    private ResultatAnalyse resultatAnalyse;

    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PanelisteCampagne> panelisteCampagnes = new ArrayList<>();


    // Helper method to add a PanelisteCampagne
    public void addPanelisteCampagne(PanelisteCampagne panelisteCampagne) {
        if (!panelisteCampagnes.contains(panelisteCampagne)) {
            panelisteCampagnes.add(panelisteCampagne);
            panelisteCampagne.setCampagne(this);
        }
    }

    // Helper method to remove a PanelisteCampagne
    public void removePanelisteCampagne(PanelisteCampagne panelisteCampagne) {
        panelisteCampagnes.remove(panelisteCampagne);
        panelisteCampagne.setCampagne(null);
    }

    // Helper method to manage the bidirectional relationship with Produit
    public void setProduit(Produit produit) {
        if (produit == null) {
            if (this.produit != null) {
                this.produit.setCampagne(null);
            }
        } else {
            produit.setCampagne(this);
        }
        this.produit = produit;
    }

}
