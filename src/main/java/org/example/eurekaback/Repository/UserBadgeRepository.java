package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserId(Long userId);

    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, String badgeId);

    @Query("SELECT ub FROM UserBadge ub WHERE ub.userId = :userId ORDER BY ub.unlockedAt DESC")
    List<UserBadge> findByUserIdOrderByUnlockedAtDesc(@Param("userId") Long userId);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.userId = :userId")
    Integer countByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndBadgeId(Long userId, String badgeId);
}
