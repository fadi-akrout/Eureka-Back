-- Migration: V3__Create_Gamification_Tables.sql
-- Create gamification system tables for PostgreSQL

-- Create ENUM types first
CREATE TYPE gamification_level AS ENUM ('BRONZE', 'ARGENT', 'OR', 'DIAMANT');
CREATE TYPE gamification_event_type AS ENUM ('PARTICIPATION', 'LEVEL_UP', 'BADGE_UNLOCKED', 'REWARD_UNLOCKED');

-- Create user_gamification_profiles table
CREATE TABLE user_gamification_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    participation_count INTEGER DEFAULT 0 NOT NULL,
    current_level gamification_level DEFAULT 'BRONZE',
    level_updated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_gamification_profile UNIQUE (user_id)
);

-- Create indexes for user_gamification_profiles
CREATE INDEX idx_participation_count ON user_gamification_profiles (participation_count);
CREATE INDEX idx_current_level ON user_gamification_profiles (current_level);

-- Create user_badges table
CREATE TABLE user_badges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    participation_count_when_unlocked INTEGER NOT NULL,

    CONSTRAINT uk_user_badge UNIQUE (user_id, badge_id)
);

-- Create indexes for user_badges
CREATE INDEX idx_user_badges_unlocked_at ON user_badges (unlocked_at);
CREATE INDEX idx_user_badges_user_id ON user_badges (user_id);

-- Create user_rewards table
CREATE TABLE user_rewards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reward_id VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    participation_count_when_unlocked INTEGER NOT NULL,
    is_claimed BOOLEAN DEFAULT FALSE,
    claimed_at TIMESTAMP NULL,

    CONSTRAINT uk_user_reward UNIQUE (user_id, reward_id)
);

-- Create indexes for user_rewards
CREATE INDEX idx_user_rewards_unlocked_at ON user_rewards (unlocked_at);
CREATE INDEX idx_user_rewards_is_claimed ON user_rewards (is_claimed);
CREATE INDEX idx_user_rewards_user_id ON user_rewards (user_id);

-- Create gamification_events table
CREATE TABLE gamification_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type gamification_event_type NOT NULL,
    campaign_id BIGINT NULL,
    old_level gamification_level NULL,
    new_level gamification_level NULL,
    badge_id VARCHAR(50) NULL,
    reward_id VARCHAR(50) NULL,
    participation_count_at_event INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for gamification_events
CREATE INDEX idx_gamification_events_user_events ON gamification_events (user_id, created_at);
CREATE INDEX idx_gamification_events_event_type ON gamification_events (event_type);
CREATE INDEX idx_gamification_events_created_at ON gamification_events (created_at);

-- Create function to automatically update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for automatic updated_at
CREATE TRIGGER update_user_gamification_profiles_updated_at
    BEFORE UPDATE ON user_gamification_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
