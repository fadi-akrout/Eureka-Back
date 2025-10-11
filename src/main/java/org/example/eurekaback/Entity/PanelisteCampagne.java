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
@Table(name = "PanelisteCampagne")
public class PanelisteCampagne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paneliste_id")
    @JsonIgnore
    private Paneliste paneliste;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campagne_id")
    @JsonIgnore
    private Campagne campagne;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutParticipation statut = StatutParticipation.INVITE;

    @Column(name = "date_invitation")
    private LocalDateTime dateInvitation;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    @Column(name = "date_completion")
    private LocalDateTime dateCompletion;

    @Column(name = "date_request")
    private LocalDateTime dateRequest;

    @Column(name = "date_approval")
    private LocalDateTime dateApproval;

    @Column(name = "feedback")
    private String feedback;

    // Enum for participation status
    public enum StatutParticipation {
        PENDING,     // New status for pending approval
        INVITE,      // User was invited but hasn't responded
        ACCEPTE,     // User accepted the invitation
        REFUSE,      // User refused the invitation
        EN_COURS,    // User is currently participating
        COMPLETE     // User completed the campaign
    }
}
