package org.example.eurekaback.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.*;
import org.example.eurekaback.Entity.*;
import org.example.eurekaback.Repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserGamificationProfileRepository profileRepository;
    private final UserBadgeRepository badgeRepository;
    private final UserRewardRepository rewardRepository;
    private final GamificationEventRepository eventRepository;
    private final PanelisteCampagneRepository participationRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Get complete gamification profile for a user
     */
    public GamificationProfileDTO getGamificationProfile(Long userId) {
        // Get or create profile
        UserGamificationProfile profile = getOrCreateProfile(userId);

        // Calculate current participation count from actual data
        Long actualParticipationCount = participationRepository.countParticipatedCampaignsByPanelisteId(userId.intValue());
        int participationCount = actualParticipationCount != null ? actualParticipationCount.intValue() : 0;

        // Update profile if participation count changed
        if (!profile.getParticipationCount().equals(participationCount)) {
            updateParticipationCount(userId, participationCount);
            // Reload profile from database to get updated values
            profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found after update"));
        }

        // Build complete profile
        return GamificationProfileDTO.builder()
            .userId(userId)
            .participationsCount(participationCount)
            .level(profile.getCurrentLevel())
            .currentLevelProgress(profile.getCurrentLevel().calculateLevelProgress(participationCount))
            .nextLevelThreshold(getNextLevelThreshold(profile.getCurrentLevel()))
            .participationsToNextLevel(profile.getCurrentLevel().getParticipationsToNextLevel(participationCount))
            .badges(getUserBadges(userId, participationCount))
            .rewards(getUserRewards(userId, participationCount))
            .build();
    }

    /**
     * Record a new participation and update gamification data
     */
    public ParticipationResult recordParticipation(Long userId, Long campaignId) {
        UserGamificationProfile profile = getOrCreateProfile(userId);
        int oldParticipationCount = profile.getParticipationCount();
        GamificationLevel oldLevel = profile.getCurrentLevel();

        // Increment participation count
        int newParticipationCount = oldParticipationCount + 1;

        // Check for level up
        GamificationLevel newLevel = GamificationLevel.calculateLevel(newParticipationCount);
        boolean levelChanged = !oldLevel.equals(newLevel);

        LocalDateTime now = LocalDateTime.now();

        // Use custom repository method with explicit casting to avoid type mismatch
        if (levelChanged) {
            profileRepository.updateGamificationProfile(
                userId,
                newLevel.name(),
                now,
                newParticipationCount,
                now
            );
        } else {
            // Only update participation count and updated_at
            profileRepository.updateGamificationProfile(
                userId,
                oldLevel.name(),
                profile.getLevelUpdatedAt(),
                newParticipationCount,
                now
            );
        }

        // DO NOT MODIFY THE PROFILE OBJECT - it will trigger automatic save by Hibernate
        // Just use the values for processing

        // Process newly unlocked badges
        List<BadgeDTO> newBadges = processNewBadges(userId, oldParticipationCount, newParticipationCount);

        // Process newly unlocked rewards
        List<RewardDTO> newRewards = processNewRewards(userId, oldParticipationCount, newParticipationCount);

        // Log events
        logParticipationEvent(userId, campaignId, newParticipationCount);
        if (levelChanged) {
            logLevelUpEvent(userId, oldLevel, newLevel, newParticipationCount);
        }

        String message = levelChanged ?
            String.format("Congratulations! Level up to %s!", newLevel.getDisplayName()) :
            "Participation recorded successfully";

        return ParticipationResult.builder()
            .success(true)
            .newParticipationCount(newParticipationCount)
            .levelChanged(levelChanged)
            .oldLevel(levelChanged ? oldLevel : null)
            .newLevel(levelChanged ? newLevel : null)
            .newlyUnlockedBadges(newBadges)
            .newlyUnlockedRewards(newRewards)
            .message(message)
            .build();
    }

    /**
     * Get leaderboard with rankings
     */
    public LeaderboardDTO getLeaderboard(int limit, int offset, Long currentUserId) {
        // Get top users
        List<UserGamificationProfile> topProfiles = profileRepository
            .findTopByParticipationCount(PageRequest.of(offset / limit, limit));

        List<LeaderboardEntry> leaderboard = topProfiles.stream()
            .map(profile -> {
                Utilisateur user = utilisateurRepository.findById(profile.getUserId().intValue()).orElse(null);
                return LeaderboardEntry.builder()
                    .userId(profile.getUserId())
                    .username(user != null ? user.getNom() : "Unknown")
                    .participationsCount(profile.getParticipationCount())
                    .level(profile.getCurrentLevel())
                    .avatarUrl(null) // TODO: Add avatar URL when available
                    .build();
            })
            .collect(Collectors.toList());

        // Calculate ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(offset + i + 1);
        }

        // Get current user position
        CurrentUserPosition currentUserPosition = null;
        if (currentUserId != null) {
            Integer userRank = profileRepository.getUserRank(currentUserId);
            UserGamificationProfile userProfile = profileRepository.findByUserId(currentUserId).orElse(null);
            if (userProfile != null) {
                currentUserPosition = CurrentUserPosition.builder()
                    .userId(currentUserId)
                    .rank(userRank)
                    .participationsCount(userProfile.getParticipationCount())
                    .build();
            }
        }

        long totalUsers = profileRepository.count();

        return LeaderboardDTO.builder()
            .leaderboard(leaderboard)
            .currentUser(currentUserPosition)
            .totalUsers(totalUsers)
            .build();
    }

    /**
     * Claim a reward
     */
    public RewardClaimResult claimReward(Long userId, String rewardId) {
        log.info("=== CLAIM REWARD SERVICE ===");
        log.info("UserId: {}, RewardId: {}", userId, rewardId);

        Optional<UserReward> userRewardOpt = rewardRepository.findByUserIdAndRewardId(userId, rewardId);
        log.info("Database query result - UserReward found: {}", userRewardOpt.isPresent());

        if (userRewardOpt.isEmpty()) {
            log.warn("Reward not found in database for userId: {} and rewardId: {}", userId, rewardId);

            // Check if reward exists in enum
            boolean rewardExistsInEnum = Arrays.stream(GamificationReward.values())
                .anyMatch(r -> r.getId().equals(rewardId));
            log.info("Reward exists in enum: {}", rewardExistsInEnum);

            if (!rewardExistsInEnum) {
                log.error("Reward ID '{}' does not exist in GamificationReward enum", rewardId);
                return RewardClaimResult.builder()
                    .success(false)
                    .message("Invalid reward ID")
                    .build();
            }

            // Check user's current participation count
            Long actualParticipationCount = participationRepository.countParticipatedCampaignsByPanelisteId(userId.intValue());
            int participationCount = actualParticipationCount != null ? actualParticipationCount.intValue() : 0;
            log.info("User's current participation count: {}", participationCount);

            // Check if user should have this reward unlocked
            GamificationReward rewardEnum = Arrays.stream(GamificationReward.values())
                .filter(r -> r.getId().equals(rewardId))
                .findFirst()
                .orElse(null);

            if (rewardEnum != null && participationCount >= rewardEnum.getRequiredParticipations()) {
                log.info("User should have this reward unlocked. Creating missing reward entry...");

                // Create the missing reward entry
                UserReward userReward = new UserReward();
                userReward.setUserId(userId);
                userReward.setRewardId(rewardId);
                userReward.setParticipationCountWhenUnlocked(participationCount);
                userReward.setUnlockedAt(LocalDateTime.now());
                userReward.setIsClaimed(false);
                userReward = rewardRepository.save(userReward);

                log.info("Created missing reward entry with ID: {}", userReward.getId());

                // Log the reward creation event
                logRewardUnlockedEvent(userId, rewardId, participationCount);

                // Now proceed with claiming the reward
                userReward.setIsClaimed(true);
                userReward.setClaimedAt(LocalDateTime.now());
                UserReward savedReward = rewardRepository.save(userReward);

                log.info("Reward claimed and saved - ID: {}, ClaimedAt: {}", savedReward.getId(), savedReward.getClaimedAt());

                // Convert to DTO
                RewardDTO rewardDTO = RewardDTO.builder()
                    .id(rewardEnum.getId())
                    .name(rewardEnum.getName())
                    .description(rewardEnum.getDescription())
                    .value(rewardEnum.getValue())
                    .currency(rewardEnum.getCurrency())
                    .isUnlocked(true)
                    .isClaimed(true)
                    .claimedAt(savedReward.getClaimedAt())
                    .build();

                log.info("Created RewardDTO: {}", rewardDTO);

                RewardClaimResult result = RewardClaimResult.builder()
                    .success(true)
                    .reward(rewardDTO)
                    .message("Reward claimed successfully")
                    .build();

                log.info("Returning successful result: {}", result);
                return result;
            } else {
                log.warn("User does not have enough participations. Required: {}, Actual: {}",
                    rewardEnum != null ? rewardEnum.getRequiredParticipations() : "unknown", participationCount);
            }

            // Check if user has any rewards at all
            List<UserReward> allUserRewards = rewardRepository.findByUserId(userId);
            log.info("User has {} total rewards in database", allUserRewards.size());
            allUserRewards.forEach(reward ->
                log.info("- Reward: {}, Claimed: {}",
                    reward.getRewardId(), reward.getIsClaimed()));

            return RewardClaimResult.builder()
                .success(false)
                .message("Reward not found or not unlocked")
                .build();
        }

        UserReward userReward = userRewardOpt.get();
        log.info("UserReward details - ID: {}, RewardId: {}, IsClaimed: {}, UnlockedAt: {}, ClaimedAt: {}",
            userReward.getId(), userReward.getRewardId(), userReward.getIsClaimed(),
            userReward.getUnlockedAt(), userReward.getClaimedAt());

        if (userReward.getIsClaimed()) {
            log.warn("Reward already claimed - RewardId: {}, ClaimedAt: {}", rewardId, userReward.getClaimedAt());
            return RewardClaimResult.builder()
                .success(false)
                .message("Reward already claimed")
                .build();
        }

        // If UserReward exists in database, it means it's unlocked
        // No need to check isUnlocked since existence = unlocked
        log.info("Reward is valid for claiming - proceeding with claim");

        // Claim the reward
        userReward.setIsClaimed(true);
        userReward.setClaimedAt(LocalDateTime.now());
        UserReward savedReward = rewardRepository.save(userReward);

        log.info("Reward claimed and saved - ID: {}, ClaimedAt: {}", savedReward.getId(), savedReward.getClaimedAt());

        // Convert to DTO
        GamificationReward rewardEnum = Arrays.stream(GamificationReward.values())
            .filter(r -> r.getId().equals(rewardId))
            .findFirst()
            .orElse(null);

        RewardDTO rewardDTO = null;
        if (rewardEnum != null) {
            rewardDTO = RewardDTO.builder()
                .id(rewardEnum.getId())
                .name(rewardEnum.getName())
                .description(rewardEnum.getDescription())
                .value(rewardEnum.getValue())
                .currency(rewardEnum.getCurrency())
                .isUnlocked(true)
                .isClaimed(true)
                .claimedAt(userReward.getClaimedAt())
                .build();
            log.info("Created RewardDTO: {}", rewardDTO);
        } else {
            log.warn("Could not find reward enum for rewardId: {}", rewardId);
        }

        RewardClaimResult result = RewardClaimResult.builder()
            .success(true)
            .reward(rewardDTO)
            .message("Reward claimed successfully")
            .build();

        log.info("Returning successful result: {}", result);
        return result;
    }

    /**
     * Process newly unlocked badges
     */
    private List<BadgeDTO> processNewBadges(Long userId, int oldCount, int newCount) {
        List<GamificationBadge> newBadges = GamificationBadge.getNewlyUnlockedBadges(oldCount, newCount);
        List<BadgeDTO> badgeDTOs = new ArrayList<>();

        for (GamificationBadge badge : newBadges) {
            // Check if badge already exists (shouldn't happen, but safety check)
            if (!badgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                UserBadge userBadge = new UserBadge();
                userBadge.setUserId(userId);
                userBadge.setBadgeId(badge.getId());
                userBadge.setParticipationCountWhenUnlocked(newCount);
                userBadge.setUnlockedAt(LocalDateTime.now());
                badgeRepository.save(userBadge);

                logBadgeUnlockedEvent(userId, badge.getId(), newCount);

                badgeDTOs.add(BadgeDTO.builder()
                    .id(badge.getId())
                    .name(badge.getName())
                    .description(badge.getDescription())
                    .requiredParticipations(badge.getRequiredParticipations())
                    .iconClass(badge.getIconClass())
                    .isUnlocked(true)
                    .unlockedAt(userBadge.getUnlockedAt())
                    .build());
            }
        }

        return badgeDTOs;
    }

    /**
     * Process newly unlocked rewards
     */
    private List<RewardDTO> processNewRewards(Long userId, int oldCount, int newCount) {
        List<GamificationReward> newRewards = GamificationReward.getNewlyUnlockedRewards(oldCount, newCount);
        List<RewardDTO> rewardDTOs = new ArrayList<>();

        for (GamificationReward reward : newRewards) {
            // Check if reward already exists (shouldn't happen, but safety check)
            if (!rewardRepository.existsByUserIdAndRewardId(userId, reward.getId())) {
                UserReward userReward = new UserReward();
                userReward.setUserId(userId);
                userReward.setRewardId(reward.getId());
                userReward.setParticipationCountWhenUnlocked(newCount);
                userReward.setUnlockedAt(LocalDateTime.now());
                userReward.setIsClaimed(false);
                rewardRepository.save(userReward);

                logRewardUnlockedEvent(userId, reward.getId(), newCount);

                rewardDTOs.add(RewardDTO.builder()
                    .id(reward.getId())
                    .name(reward.getName())
                    .description(reward.getDescription())
                    .requiredParticipations(reward.getRequiredParticipations())
                    .value(reward.getValue())
                    .currency(reward.getCurrency())
                    .isUnlocked(true)
                    .isClaimed(false)
                    .unlockedAt(userReward.getUnlockedAt())
                    .build());
            }
        }

        return rewardDTOs;
    }

    /**
     * Get user badges with unlock status
     */
    private List<BadgeDTO> getUserBadges(Long userId, int participationCount) {
        List<UserBadge> userBadges = badgeRepository.findByUserId(userId);
        Map<String, UserBadge> userBadgeMap = userBadges.stream()
            .collect(Collectors.toMap(UserBadge::getBadgeId, ub -> ub));

        return Arrays.stream(GamificationBadge.values())
            .map(badge -> {
                UserBadge userBadge = userBadgeMap.get(badge.getId());
                boolean isUnlocked = participationCount >= badge.getRequiredParticipations();

                return BadgeDTO.builder()
                    .id(badge.getId())
                    .name(badge.getName())
                    .description(badge.getDescription())
                    .requiredParticipations(badge.getRequiredParticipations())
                    .iconClass(badge.getIconClass())
                    .isUnlocked(isUnlocked)
                    .unlockedAt(userBadge != null ? userBadge.getUnlockedAt() : null)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Get user rewards with unlock and claim status
     */
    private List<RewardDTO> getUserRewards(Long userId, int participationCount) {
        List<UserReward> userRewards = rewardRepository.findByUserId(userId);
        Map<String, UserReward> userRewardMap = userRewards.stream()
            .collect(Collectors.toMap(UserReward::getRewardId, ur -> ur));

        return Arrays.stream(GamificationReward.values())
            .map(reward -> {
                UserReward userReward = userRewardMap.get(reward.getId());
                boolean isUnlocked = participationCount >= reward.getRequiredParticipations();

                return RewardDTO.builder()
                    .id(reward.getId())
                    .name(reward.getName())
                    .description(reward.getDescription())
                    .requiredParticipations(reward.getRequiredParticipations())
                    .value(reward.getValue())
                    .currency(reward.getCurrency())
                    .isUnlocked(isUnlocked)
                    .isClaimed(userReward != null ? userReward.getIsClaimed() : false)
                    .unlockedAt(userReward != null ? userReward.getUnlockedAt() : null)
                    .claimedAt(userReward != null ? userReward.getClaimedAt() : null)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Get or create gamification profile for user
     */
    private UserGamificationProfile getOrCreateProfile(Long userId) {
        return profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Count existing participations
                Long participationCount = participationRepository.countParticipatedCampaignsByPanelisteId(userId.intValue());
                int count = participationCount != null ? participationCount.intValue() : 0;

                GamificationLevel level = GamificationLevel.calculateLevel(count);
                LocalDateTime now = LocalDateTime.now();

                // Use native SQL insert to avoid enum type issues
                profileRepository.createGamificationProfile(userId, count, level.name(), now, now);

                // Retrieve the created profile from database to get proper ID and avoid Hibernate issues
                return profileRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Failed to create gamification profile"));
            });
    }

    /**
     * Update participation count and level for a user using safe method
     */
    private void updateParticipationCount(Long userId, int newParticipationCount) {
        UserGamificationProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile != null) {
            GamificationLevel oldLevel = profile.getCurrentLevel();
            GamificationLevel newLevel = GamificationLevel.calculateLevel(newParticipationCount);
            LocalDateTime now = LocalDateTime.now();

            // Use custom repository method with explicit casting - NO PROFILE MODIFICATION
            if (!oldLevel.equals(newLevel)) {
                profileRepository.updateGamificationProfile(
                    userId,
                    newLevel.name(),
                    now,
                    newParticipationCount,
                    now
                );
            } else {
                profileRepository.updateGamificationProfile(
                    userId,
                    oldLevel.name(),
                    profile.getLevelUpdatedAt(),
                    newParticipationCount,
                    now
                );
            }

            // DO NOT MODIFY THE PROFILE OBJECT - it will trigger automatic save by Hibernate
            // The database has been updated via custom queries with explicit casting
        }
    }

    /**
     * Get next level threshold
     */
    private Integer getNextLevelThreshold(GamificationLevel currentLevel) {
        switch (currentLevel) {
            case BRONZE: return GamificationLevel.ARGENT.getRequiredParticipations();
            case ARGENT: return GamificationLevel.OR.getRequiredParticipations();
            case OR: return GamificationLevel.DIAMANT.getRequiredParticipations();
            case DIAMANT: return null; // Max level
            default: return null;
        }
    }

    // Event logging methods
    private void logParticipationEvent(Long userId, Long campaignId, int participationCount) {
        GamificationEvent event = new GamificationEvent();
        event.setUserId(userId);
        event.setEventType(GamificationEvent.GamificationEventType.PARTICIPATION);
        event.setCampaignId(campaignId);
        event.setParticipationCountAtEvent(participationCount);
        eventRepository.save(event);
    }

    private void logLevelUpEvent(Long userId, GamificationLevel oldLevel, GamificationLevel newLevel, int participationCount) {
        GamificationEvent event = new GamificationEvent();
        event.setUserId(userId);
        event.setEventType(GamificationEvent.GamificationEventType.LEVEL_UP);
        event.setOldLevel(oldLevel);
        event.setNewLevel(newLevel);
        event.setParticipationCountAtEvent(participationCount);
        eventRepository.save(event);
    }

    private void logBadgeUnlockedEvent(Long userId, String badgeId, int participationCount) {
        GamificationEvent event = new GamificationEvent();
        event.setUserId(userId);
        event.setEventType(GamificationEvent.GamificationEventType.BADGE_UNLOCKED);
        event.setBadgeId(badgeId);
        event.setParticipationCountAtEvent(participationCount);
        eventRepository.save(event);
    }

    private void logRewardUnlockedEvent(Long userId, String rewardId, int participationCount) {
        GamificationEvent event = new GamificationEvent();
        event.setUserId(userId);
        event.setEventType(GamificationEvent.GamificationEventType.REWARD_UNLOCKED);
        event.setRewardId(rewardId);
        event.setParticipationCountAtEvent(participationCount);
        eventRepository.save(event);
    }
}
