package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int note;
    private String commentaire;
    private Date date;

    @ManyToOne
    @JoinColumn(name = "paneliste_id")
    @JsonIgnore
    private Paneliste paneliste;

    @ManyToOne
    @JoinColumn(name = "campagne_id")
    @JsonIgnore
    private Campagne campagne;
}
