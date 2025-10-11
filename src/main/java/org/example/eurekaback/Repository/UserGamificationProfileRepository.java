package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.GamificationLevel;
import org.example.eurekaback.Entity.UserGamificationProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserGamificationProfileRepository extends JpaRepository<UserGamificationProfile, Long> {

    Optional<UserGamificationProfile> findByUserId(Long userId);

    @Query("SELECT ugp FROM UserGamificationProfile ugp ORDER BY ugp.participationCount DESC, ugp.createdAt ASC")
    List<UserGamificationProfile> findTopByParticipationCount(Pageable pageable);

    @Query("SELECT COUNT(ugp) + 1 FROM UserGamificationProfile ugp WHERE ugp.participationCount > " +
           "(SELECT ugp2.participationCount FROM UserGamificationProfile ugp2 WHERE ugp2.userId = :userId)")
    Integer getUserRank(@Param("userId") Long userId);

    @Query("SELECT COUNT(ugp) FROM UserGamificationProfile ugp WHERE ugp.participationCount >= :participationCount")
    Long countUsersWithParticipationCountGreaterOrEqual(@Param("participationCount") Integer participationCount);

    @Modifying
    @Query(value = "UPDATE user_gamification_profiles SET current_level = CAST(:level AS gamification_level), " +
                   "level_updated_at = :levelUpdatedAt, participation_count = :participationCount, " +
                   "updated_at = :updatedAt WHERE user_id = :userId", nativeQuery = true)
    int updateGamificationProfile(@Param("userId") Long userId,
                                 @Param("level") String level,
                                 @Param("levelUpdatedAt") LocalDateTime levelUpdatedAt,
                                 @Param("participationCount") Integer participationCount,
                                 @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = "UPDATE user_gamification_profiles SET current_level = CAST(:level AS gamification_level), " +
                   "level_updated_at = :levelUpdatedAt, updated_at = :updatedAt WHERE user_id = :userId",
                   nativeQuery = true)
    int updateUserLevel(@Param("userId") Long userId,
                       @Param("level") String level,
                       @Param("levelUpdatedAt") LocalDateTime levelUpdatedAt,
                       @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = "INSERT INTO user_gamification_profiles (user_id, participation_count, current_level, created_at, updated_at) " +
                   "VALUES (:userId, :participationCount, CAST(:level AS gamification_level), :createdAt, :updatedAt)",
                   nativeQuery = true)
    int createGamificationProfile(@Param("userId") Long userId,
                                 @Param("participationCount") Integer participationCount,
                                 @Param("level") String level,
                                 @Param("createdAt") LocalDateTime createdAt,
                                 @Param("updatedAt") LocalDateTime updatedAt);
}
