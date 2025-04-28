package org.example.eurekaback.Entity;
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

public class Annonceur extends Utilisateur {
    private String entreprise;

    @OneToMany(mappedBy = "annonceur", cascade = CascadeType.ALL)
    private List<Campagne> campagnesCreees = new ArrayList<>();
}
