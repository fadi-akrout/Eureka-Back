package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Produit")
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String description;
    private String categorie;
    private float noteMoyenne;
    @OneToOne
    @JoinColumn(name = "campagne_id", unique = true) // Ensures one-to-one relationship
    @JsonIgnore
    private Campagne campagne;
}
