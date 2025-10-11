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
@Table(name = "user_badges",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"}))
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_id", nullable = false, length = 50)
    private String badgeId;

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    @Column(name = "participation_count_when_unlocked", nullable = false)
    private Integer participationCountWhenUnlocked;

    @PrePersist
    protected void onCreate() {
        if (this.unlockedAt == null) {
            this.unlockedAt = LocalDateTime.now();
        }
    }
}
