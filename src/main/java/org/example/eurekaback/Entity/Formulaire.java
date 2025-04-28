package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Formulaire")
public class Formulaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "campagne_id")
    @JsonIgnore
    private Campagne campagne;

    @OneToMany(mappedBy = "formulaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormulaireQuestion> formulaireQuestions = new ArrayList<>();
}
