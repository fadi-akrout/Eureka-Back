package org.example.eurekaback.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "reponse")
public class Reponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private Date date;
    
    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;
    
    @Column(name = "paneliste_id")
    private Integer panelisteId;
    
    @Column(name = "formulaire_id")
    private Integer formulaireId;
    
    // For storing selected option IDs
    @ElementCollection
    @CollectionTable(name = "reponse_options", joinColumns = @JoinColumn(name = "reponse_id"))
    @Column(name = "option_id")
    private List<Long> optionIds = new ArrayList<>();
}
