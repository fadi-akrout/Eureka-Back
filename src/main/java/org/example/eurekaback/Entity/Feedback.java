package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer note; // Rating 1-5
    
    @Column(nullable = false, length = 1000)
    private String commentaire;
    
    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "paneliste_id", nullable = false)
    @JsonIgnore
    private Paneliste paneliste;

    @ManyToOne
    @JoinColumn(name = "campagne_id", nullable = false)
    @JsonIgnore
    private Campagne campagne;
}
