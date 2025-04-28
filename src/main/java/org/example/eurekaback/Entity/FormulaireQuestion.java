package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "FormulaireQuestion")
public class FormulaireQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "formulaire_id")
    @JsonIgnore
    private Formulaire formulaire;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;
}
