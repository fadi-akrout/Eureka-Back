-- Migration script to add sentiment analysis columns to resultatanalyse table
-- Version: V2__Add_Sentiment_Analysis_Columns.sql

-- Add sentiment analysis columns to existing resultatanalyse table
ALTER TABLE resultatanalyse ADD COLUMN IF NOT EXISTS sentiment_analysis_data JSON;
ALTER TABLE resultatanalyse ADD COLUMN IF NOT EXISTS sentiment_analysis_summary JSON;
ALTER TABLE resultatanalyse ADD COLUMN IF NOT EXISTS sentiment_last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE resultatanalyse ADD COLUMN IF NOT EXISTS sentiment_total_feedbacks INTEGER DEFAULT 0;
ALTER TABLE resultatanalyse ADD COLUMN IF NOT EXISTS sentiment_analyzed_feedbacks INTEGER DEFAULT 0;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_resultatanalyse_sentiment_updated ON resultatanalyse(sentiment_last_updated);

-- Add constraints with DO block to check if they exist
DO $$
BEGIN
    -- Check if constraint already exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_sentiment_feedbacks_positive' 
        AND conrelid = 'resultatanalyse'::regclass
    ) THEN
        ALTER TABLE resultatanalyse ADD CONSTRAINT chk_sentiment_feedbacks_positive 
          CHECK (sentiment_total_feedbacks >= 0 AND sentiment_analyzed_feedbacks >= 0);
    END IF;
END $$;

DO $$
BEGIN
    -- Check if constraint already exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_sentiment_analyzed_not_greater_than_total' 
        AND conrelid = 'resultatanalyse'::regclass
    ) THEN
        ALTER TABLE resultatanalyse ADD CONSTRAINT chk_sentiment_analyzed_not_greater_than_total 
          CHECK (sentiment_analyzed_feedbacks <= sentiment_total_feedbacks);
    END IF;
END $$;

-- Add comments for documentation
COMMENT ON COLUMN resultatanalyse.sentiment_analysis_data IS 'JSON data containing detailed sentiment analysis results for individual feedbacks';
COMMENT ON COLUMN resultatanalyse.sentiment_analysis_summary IS 'JSON data containing aggregated sentiment analysis summary and insights';
COMMENT ON COLUMN resultatanalyse.sentiment_last_updated IS 'Timestamp of when sentiment analysis was last performed';
COMMENT ON COLUMN resultatanalyse.sentiment_total_feedbacks IS 'Total number of feedbacks for the campaign';
COMMENT ON COLUMN resultatanalyse.sentiment_analyzed_feedbacks IS 'Number of feedbacks that have been analyzed for sentiment';