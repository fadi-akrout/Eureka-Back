package org.example.eurekaback.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eurekaback.Entity.Campagne;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampagneDTO {
    private int id;
    private String nom;
    private String statut;
    private Date dateDebut;
    private Date dateFin;
    private String description;
    private Integer annonceurId;

    // Constructor to create DTO from Campagne entity
    public CampagneDTO(Campagne campagne) {
        this.id = campagne.getId();
        this.nom = campagne.getNom();
        this.statut = campagne.getStatut();
        this.dateDebut = campagne.getDateDebut();
        this.dateFin = campagne.getDateFin();
        this.description = campagne.getDescription();
        this.annonceurId = campagne.getAnnonceurId();
    }
}