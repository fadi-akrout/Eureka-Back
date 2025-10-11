package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Transient;
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
@Table(name = "campagne")
public class Campagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nom;
    @Column(columnDefinition = "TEXT")
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateFin;
    private String statut;
    
    @Column(columnDefinition = "TEXT")
    private String imageUrl; // URL to the stored image

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonceur_id")
    @JsonIgnore  // Ignore the full annonceur object in JSON serialization
    private Annonceur annonceur;
    
    // Transient field to store annonceurId for serialization
    @Transient
    private Integer annonceurId;
    
    // Method to get annonceurId for frontend compatibility
    public Integer getAnnonceurId() {
        // First try to get from the annonceur object
        if (annonceur != null) {
            return annonceur.getId();
        }
        // Fall back to the transient field
        return annonceurId;
    }
    
    // This method is used for JSON deserialization and by the service layer
    public void setAnnonceurId(Integer annonceurId) {
        this.annonceurId = annonceurId;
    }

    @OneToOne(mappedBy = "campagne", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Produit produit;

    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToOne(mappedBy = "campagne", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Formulaire formulaire;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "resultat_analyse_id", unique = true, nullable = true)
    @JsonIgnore
    private ResultatAnalyse resultatAnalyse;

    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
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
