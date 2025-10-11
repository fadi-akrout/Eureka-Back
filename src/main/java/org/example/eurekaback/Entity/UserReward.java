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
@Table(name = "user_rewards",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "reward_id"}))
public class UserReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reward_id", nullable = false, length = 50)
    private String rewardId;

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    @Column(name = "participation_count_when_unlocked", nullable = false)
    private Integer participationCountWhenUnlocked;

    @Column(name = "is_claimed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isClaimed = false;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @PrePersist
    protected void onCreate() {
        if (this.unlockedAt == null) {
            this.unlockedAt = LocalDateTime.now();
        }
    }
}
