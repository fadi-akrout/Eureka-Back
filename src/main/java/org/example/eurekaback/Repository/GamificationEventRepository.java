package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.GamificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GamificationEventRepository extends JpaRepository<GamificationEvent, Long> {

    List<GamificationEvent> findByUserId(Long userId);

    @Query("SELECT ge FROM GamificationEvent ge WHERE ge.userId = :userId ORDER BY ge.createdAt DESC")
    List<GamificationEvent> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT ge FROM GamificationEvent ge WHERE ge.eventType = :eventType ORDER BY ge.createdAt DESC")
    List<GamificationEvent> findByEventTypeOrderByCreatedAtDesc(@Param("eventType") GamificationEvent.GamificationEventType eventType);

    @Query("SELECT ge FROM GamificationEvent ge WHERE ge.userId = :userId AND ge.eventType = :eventType")
    List<GamificationEvent> findByUserIdAndEventType(@Param("userId") Long userId, @Param("eventType") GamificationEvent.GamificationEventType eventType);

    @Query("SELECT COUNT(ge) FROM GamificationEvent ge WHERE ge.eventType = 'PARTICIPATION' AND ge.createdAt >= :startDate")
    Long countParticipationEventsSince(@Param("startDate") LocalDateTime startDate);
}
