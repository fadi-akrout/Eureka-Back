package org.example.eurekaback.Entity;

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
@Table(name = "gamification_events")
public class GamificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private GamificationEventType eventType;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_level")
    private GamificationLevel oldLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_level")
    private GamificationLevel newLevel;

    @Column(name = "badge_id", length = 50)
    private String badgeId;

    @Column(name = "reward_id", length = 50)
    private String rewardId;

    @Column(name = "participation_count_at_event", nullable = false)
    private Integer participationCountAtEvent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum GamificationEventType {
        PARTICIPATION,
        LEVEL_UP,
        BADGE_UNLOCKED,
        REWARD_UNLOCKED
    }
}
