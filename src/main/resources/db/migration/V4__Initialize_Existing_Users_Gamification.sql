-- Migration: V4__Initialize_Existing_Users_Gamification.sql
-- Initialize gamification data for existing users (PostgreSQL compatible)

-- Create gamification profiles for existing panelistes
INSERT INTO user_gamification_profiles (user_id, participation_count, current_level)
SELECT
    u.id::BIGINT as user_id,
    COALESCE(pc.participation_count, 0) as participation_count,
    CASE
        WHEN COALESCE(pc.participation_count, 0) >= 25 THEN 'DIAMANT'::gamification_level
        WHEN COALESCE(pc.participation_count, 0) >= 10 THEN 'OR'::gamification_level
        WHEN COALESCE(pc.participation_count, 0) >= 3 THEN 'ARGENT'::gamification_level
        ELSE 'BRONZE'::gamification_level
    END as current_level
FROM Utilisateur u
LEFT JOIN (
    SELECT
        paneliste_id,
        COUNT(*) as participation_count
    FROM paneliste_campagne
    WHERE statut IN ('ACCEPTE', 'EN_COURS', 'COMPLETE')
    GROUP BY paneliste_id
) pc ON u.id = pc.paneliste_id
WHERE u.role = 'PANELISTE'
ON CONFLICT (user_id) DO NOTHING;

-- Create badges for existing participations
INSERT INTO user_badges (user_id, badge_id, participation_count_when_unlocked)
SELECT DISTINCT
    ugp.user_id,
    badge.badge_id,
    ugp.participation_count
FROM user_gamification_profiles ugp
CROSS JOIN (
    SELECT 'first_participation' as badge_id, 1 as required_participations
    UNION SELECT 'regular_participant', 5
    UNION SELECT 'dedicated_tester', 10
    UNION SELECT 'expert_reviewer', 15
    UNION SELECT 'campaign_master', 25
    UNION SELECT 'legend', 50
) badge
WHERE ugp.participation_count >= badge.required_participations
ON CONFLICT (user_id, badge_id) DO NOTHING;

-- Create rewards for existing participations
INSERT INTO user_rewards (user_id, reward_id, participation_count_when_unlocked)
SELECT DISTINCT
    ugp.user_id,
    reward.reward_id,
    ugp.participation_count
FROM user_gamification_profiles ugp
CROSS JOIN (
    SELECT 'welcome_bonus' as reward_id, 1 as required_participations
    UNION SELECT 'loyalty_reward', 5
    UNION SELECT 'dedication_prize', 10
    UNION SELECT 'expert_bonus', 15
    UNION SELECT 'master_reward', 25
    UNION SELECT 'legendary_prize', 50
) reward
WHERE ugp.participation_count >= reward.required_participations
ON CONFLICT (user_id, reward_id) DO NOTHING;
