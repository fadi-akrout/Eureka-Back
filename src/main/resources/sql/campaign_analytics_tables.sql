-- Campaign Analytics Database Schema (PostgreSQL)
-- This script creates optional tables for storing pre-computed analytics
-- The current implementation works without these tables by computing analytics on-demand

-- Create custom types for enums
DO $$ BEGIN
    CREATE TYPE sentiment_type AS ENUM ('POSITIVE', 'NEGATIVE', 'NEUTRAL', 'MIXED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE confidence_level_type AS ENUM ('HIGH', 'MEDIUM', 'LOW');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE insight_type AS ENUM ('KEY_FINDING', 'RECOMMENDATION', 'TREND', 'RISK_FACTOR');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE priority_level_type AS ENUM ('HIGH', 'MEDIUM', 'LOW');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Campaign Analytics Summary Table (Optional - for caching)
CREATE TABLE IF NOT EXISTS campaign_analytics (
    id BIGSERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL,
    
    -- Sentiment Distribution (aggregated from existing feedback sentiment data)
    total_responses INTEGER DEFAULT 0,
    positive_count INTEGER DEFAULT 0,
    negative_count INTEGER DEFAULT 0,
    neutral_count INTEGER DEFAULT 0,
    positive_percentage DECIMAL(5,2) DEFAULT 0.00,
    negative_percentage DECIMAL(5,2) DEFAULT 0.00,
    neutral_percentage DECIMAL(5,2) DEFAULT 0.00,
    
    -- Confidence Scores (aggregated from existing confidence scores)
    average_confidence DECIMAL(5,4) DEFAULT 0.0000,
    high_confidence_count INTEGER DEFAULT 0,
    medium_confidence_count INTEGER DEFAULT 0,
    low_confidence_count INTEGER DEFAULT 0,
    high_confidence_percentage DECIMAL(5,2) DEFAULT 0.00,
    medium_confidence_percentage DECIMAL(5,2) DEFAULT 0.00,
    low_confidence_percentage DECIMAL(5,2) DEFAULT 0.00,
    
    -- Overall Analysis
    overall_sentiment sentiment_type DEFAULT 'MIXED',
    confidence_level confidence_level_type DEFAULT 'LOW',
    
    -- Additional Metrics
    response_rate DECIMAL(5,2) DEFAULT 0.00,
    completion_rate DECIMAL(5,2) DEFAULT 0.00,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    
    -- Timestamps
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_campaign_analytics_campaign FOREIGN KEY (campaign_id) REFERENCES campagne(id) ON DELETE CASCADE,
    CONSTRAINT unique_campaign_analytics UNIQUE (campaign_id)
);

-- Create index
CREATE INDEX IF NOT EXISTS idx_campaign_analytics_generated_at ON campaign_analytics(generated_at);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_campaign_analytics_updated_at ON campaign_analytics;
CREATE TRIGGER update_campaign_analytics_updated_at
    BEFORE UPDATE ON campaign_analytics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Campaign Insights Table (Optional - for storing generated insights)
CREATE TABLE IF NOT EXISTS campaign_insights (
    id BIGSERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL,
    insight_type insight_type NOT NULL,
    insight_text TEXT NOT NULL,
    confidence_score DECIMAL(5,4) DEFAULT 0.0000,
    priority_level priority_level_type DEFAULT 'MEDIUM',
    source_feedback_count INTEGER DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_campaign_insights_campaign FOREIGN KEY (campaign_id) REFERENCES campagne(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_campaign_insights_campaign_id ON campaign_insights(campaign_id);
CREATE INDEX IF NOT EXISTS idx_campaign_insights_type ON campaign_insights(insight_type);

-- Create trigger for updated_at
DROP TRIGGER IF EXISTS update_campaign_insights_updated_at ON campaign_insights;
CREATE TRIGGER update_campaign_insights_updated_at
    BEFORE UPDATE ON campaign_insights
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Campaign Keywords Table (Optional - for storing extracted keywords)
CREATE TABLE IF NOT EXISTS campaign_keywords (
    id BIGSERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    frequency INTEGER DEFAULT 1,
    sentiment_association sentiment_type DEFAULT 'MIXED',
    confidence_score DECIMAL(5,4) DEFAULT 0.0000,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_campaign_keywords_campaign FOREIGN KEY (campaign_id) REFERENCES campagne(id) ON DELETE CASCADE,
    CONSTRAINT unique_campaign_keyword UNIQUE (campaign_id, keyword)
);

-- Create index
CREATE INDEX IF NOT EXISTS idx_campaign_keywords_frequency ON campaign_keywords(frequency DESC);

-- Create trigger for updated_at
DROP TRIGGER IF EXISTS update_campaign_keywords_updated_at ON campaign_keywords;
CREATE TRIGGER update_campaign_keywords_updated_at
    BEFORE UPDATE ON campaign_keywords
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Daily Analytics Table (Optional - for time-based analysis)
CREATE TABLE IF NOT EXISTS daily_analytics (
    id BIGSERIAL PRIMARY KEY,
    campaign_id INTEGER NOT NULL,
    analysis_date DATE NOT NULL,
    
    response_count INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    dominant_sentiment sentiment_type DEFAULT 'MIXED',
    
    positive_count INTEGER DEFAULT 0,
    negative_count INTEGER DEFAULT 0,
    neutral_count INTEGER DEFAULT 0,
    
    average_confidence DECIMAL(5,4) DEFAULT 0.0000,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_daily_analytics_campaign FOREIGN KEY (campaign_id) REFERENCES campagne(id) ON DELETE CASCADE,
    CONSTRAINT unique_campaign_date UNIQUE (campaign_id, analysis_date)
);

-- Create index
CREATE INDEX IF NOT EXISTS idx_daily_analytics_date ON daily_analytics(analysis_date);

-- Note: These tables are optional. The current implementation computes analytics
-- on-demand from existing sentiment analysis data stored in the resultatanalyse table.
-- You can run this script if you want to implement caching for better performance.