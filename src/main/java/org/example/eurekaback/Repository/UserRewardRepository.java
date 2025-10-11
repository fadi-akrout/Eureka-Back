package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRewardRepository extends JpaRepository<UserReward, Long> {

    List<UserReward> findByUserId(Long userId);

    Optional<UserReward> findByUserIdAndRewardId(Long userId, String rewardId);

    @Query("SELECT ur FROM UserReward ur WHERE ur.userId = :userId ORDER BY ur.unlockedAt DESC")
    List<UserReward> findByUserIdOrderByUnlockedAtDesc(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserReward ur WHERE ur.userId = :userId AND ur.isClaimed = false")
    List<UserReward> findUnclaimedRewardsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ur) FROM UserReward ur WHERE ur.userId = :userId AND ur.isClaimed = true")
    Integer countClaimedRewardsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndRewardId(Long userId, String rewardId);
}
