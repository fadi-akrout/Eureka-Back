package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "formulaire")
public class Formulaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "campagne_id")
    @JsonIgnore
    private Campagne campagne;

    @OneToMany(mappedBy = "formulaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FormulaireQuestion> formulaireQuestions = new HashSet<>();
}
