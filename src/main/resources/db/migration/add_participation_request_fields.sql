-- Migration script to add participation request fields to PanelisteCampagne table

-- Add new columns for participation request system
ALTER TABLE paneliste_campagne 
ADD COLUMN date_request TIMESTAMP NULL,
ADD COLUMN date_approval TIMESTAMP NULL,
ADD COLUMN feedback TEXT NULL;

-- Update the statut enum to include PENDING status
-- Note: This syntax may vary depending on your database system
-- For MySQL:
ALTER TABLE paneliste_campagne 
MODIFY COLUMN statut ENUM('PENDING', 'INVITE', 'ACCEPTE', 'REFUSE', 'EN_COURS', 'COMPLETE') NOT NULL DEFAULT 'INVITE';

-- For PostgreSQL, you would need to:
-- ALTER TYPE statutparticipation ADD VALUE 'PENDING' BEFORE 'INVITE';

-- Set default values for existing records where appropriate
UPDATE paneliste_campagne 
SET date_request = date_invitation 
WHERE date_request IS NULL 
  AND date_invitation IS NOT NULL 
  AND statut IN ('ACCEPTE', 'REFUSE', 'EN_COURS', 'COMPLETE');

-- Update existing records to have proper request dates
UPDATE paneliste_campagne 
SET date_request = COALESCE(date_invitation, date_reponse, NOW())
WHERE date_request IS NULL;

-- Add indexes for better performance
CREATE INDEX idx_paneliste_campagne_statut ON paneliste_campagne(statut);
CREATE INDEX idx_paneliste_campagne_date_request ON paneliste_campagne(date_request);
CREATE INDEX idx_paneliste_campagne_campagne_statut ON paneliste_campagne(campagne_id, statut);