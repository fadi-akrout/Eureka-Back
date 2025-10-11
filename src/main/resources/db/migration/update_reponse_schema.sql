-- Add new columns to reponse table
ALTER TABLE reponse ADD COLUMN IF NOT EXISTS response_text TEXT;
ALTER TABLE reponse ADD COLUMN IF NOT EXISTS paneliste_id INTEGER;
ALTER TABLE reponse ADD COLUMN IF NOT EXISTS formulaire_id INTEGER;

-- Create table for option IDs if using a separate table approach
CREATE TABLE IF NOT EXISTS reponse_options (
    reponse_id INTEGER NOT NULL,
    option_id BIGINT NOT NULL,
    PRIMARY KEY (reponse_id, option_id),
    FOREIGN KEY (reponse_id) REFERENCES reponse(id) ON DELETE CASCADE
);

-- Add foreign key constraints - using DO block to check if constraint exists
DO $$
BEGIN
    -- Check if the constraint already exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_reponse_formulaire' 
        AND conrelid = 'reponse'::regclass
    ) THEN
        -- Add the constraint if it doesn't exist
        ALTER TABLE reponse 
        ADD CONSTRAINT fk_reponse_formulaire 
        FOREIGN KEY (formulaire_id) 
        REFERENCES formulaire(id);
    END IF;
END $$;

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_reponse_paneliste ON reponse(paneliste_id);
CREATE INDEX IF NOT EXISTS idx_reponse_formulaire ON reponse(formulaire_id);