-- SQL script to fix NULL statut values in paneliste_campagne table
-- Run this script in your PostgreSQL database before starting the application

-- Update all NULL statut values to 'INVITE' (default status)
UPDATE paneliste_campagne 
SET statut = 'INVITE' 
WHERE statut IS NULL;

-- Verify the update
SELECT COUNT(*) as null_statut_count 
FROM paneliste_campagne 
WHERE statut IS NULL;

-- Show all records to verify
SELECT id, paneliste_id, campagne_id, statut, date_invitation, date_reponse 
FROM paneliste_campagne 
ORDER BY id;