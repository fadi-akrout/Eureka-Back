package org.example.eurekaback.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.*;
import org.example.eurekaback.Service.GamificationService;
import org.example.eurekaback.Service.UserContextService;
import org.example.eurekaback.Entity.Utilisateur;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/gamification")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class GamificationController {

    private final GamificationService gamificationService;
    private final UserContextService userContextService;

    @GetMapping("/profile")
    public ResponseEntity<GamificationProfileDTO> getProfile() {
        try {
            Long userId = userContextService.getCurrentUserId();
            log.info("Getting gamification profile for user: {}", userId);

            GamificationProfileDTO profile = gamificationService.getGamificationProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error getting gamification profile", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<GamificationProfileDTO> getProfileByUserId(@PathVariable Long userId) {
        try {
            Utilisateur currentUser = userContextService.getCurrentUser();
            log.info("Getting gamification profile for userId: {} requested by user: {} with role: {}",
                    userId, currentUser.getId(), currentUser.getRole());

            // Check if user has permission to view other profiles
            if (!canViewProfile(currentUser, userId)) {
                log.warn("User {} with role {} attempted to access profile of user {} without permission",
                        currentUser.getId(), currentUser.getRole(), userId);
                return ResponseEntity.status(403).build();
            }

            GamificationProfileDTO profile = gamificationService.getGamificationProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error getting gamification profile for userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if the current user can view the profile of the specified user
     */
    private boolean canViewProfile(Utilisateur currentUser, Long targetUserId) {
        // Users can always view their own profile
        if (currentUser.getId() == targetUserId.intValue()) {
            return true;
        }

        // Admins and advertisers can view panelist profiles
        String role = currentUser.getRole();
        return "ADMIN".equals(role) || "ANNONCEUR".equals(role);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardDTO> getLeaderboard(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            Long currentUserId = userContextService.getCurrentUserId();
            log.info("Getting leaderboard with limit: {}, offset: {} for user: {}", limit, offset, currentUserId);

            LeaderboardDTO leaderboard = gamificationService.getLeaderboard(limit, offset, currentUserId);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            log.error("Error getting leaderboard", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/participation")
    public ResponseEntity<ParticipationResult> recordParticipation(
            @RequestBody ParticipationRequest request) {
        try {
            Long userId = userContextService.getCurrentUserId();
            log.info("Recording participation for user: {} in campaign: {}", userId, request.getCampaignId());

            ParticipationResult result = gamificationService.recordParticipation(userId, request.getCampaignId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error recording participation", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/rewards/{rewardId}/claim")
    public ResponseEntity<RewardClaimResult> claimReward(@PathVariable String rewardId, HttpServletRequest request) {
        try {
            Long userId = userContextService.getCurrentUserId();
            log.info("=== REWARD CLAIM REQUEST ===");
            log.info("Request URL: {}", request.getRequestURL());
            log.info("Request Method: {}", request.getMethod());
            log.info("Reward ID from path: {}", rewardId);
            log.info("User ID from context: {}", userId);
            log.info("Content-Type: {}", request.getContentType());

            // Log request body if present
            try {
                String body = request.getReader().lines().collect(java.util.stream.Collectors.joining("\n"));
                log.info("Request Body: {}", body.isEmpty() ? "EMPTY" : body);
            } catch (Exception e) {
                log.info("Could not read request body: {}", e.getMessage());
            }

            RewardClaimResult result = gamificationService.claimReward(userId, rewardId);

            log.info("Service result - Success: {}, Message: {}", result.getSuccess(), result.getMessage());

            if (result.getSuccess()) {
                log.info("Returning 200 OK with result: {}", result);
                return ResponseEntity.ok(result);
            } else {
                log.warn("Returning 400 BAD REQUEST with result: {}", result);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("Error claiming reward - Exception: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
