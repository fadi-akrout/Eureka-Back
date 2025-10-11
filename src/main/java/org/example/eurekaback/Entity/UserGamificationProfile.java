package org.example.eurekaback.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_gamification_profiles")
public class UserGamificationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "participation_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer participationCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false, columnDefinition = "gamification_level DEFAULT 'BRONZE'")
    private GamificationLevel currentLevel = GamificationLevel.BRONZE;

    @Column(name = "level_updated_at")
    private LocalDateTime levelUpdatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
