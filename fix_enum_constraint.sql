-- SQL script to fix the PostgreSQL constraint for statut in paneliste_campagne table
-- This script handles the case where Hibernate created a VARCHAR column with check constraint

-- First, let's check the current table structure
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'paneliste_campagne' 
AND column_name = 'statut';

-- Check for existing check constraints
SELECT 
    tc.constraint_name, 
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name = 'paneliste_campagne'
AND tc.constraint_type = 'CHECK';

-- Drop the existing check constraint if it exists
DO $$
DECLARE
    constraint_name_var TEXT;
BEGIN
    -- Find the constraint name for the statut column
    SELECT tc.constraint_name INTO constraint_name_var
    FROM information_schema.table_constraints tc
    JOIN information_schema.check_constraints cc ON tc.constraint_name = cc.constraint_name
    WHERE tc.table_name = 'paneliste_campagne'
    AND tc.constraint_type = 'CHECK'
    AND cc.check_clause LIKE '%statut%'
    LIMIT 1;
    
    -- Drop the constraint if found
    IF constraint_name_var IS NOT NULL THEN
        EXECUTE 'ALTER TABLE paneliste_campagne DROP CONSTRAINT ' || constraint_name_var;
        RAISE NOTICE 'Dropped constraint: %', constraint_name_var;
    END IF;
END $$;

-- Add new check constraint that includes PENDING
ALTER TABLE paneliste_campagne 
ADD CONSTRAINT paneliste_campagne_statut_check 
CHECK (statut IN ('PENDING', 'INVITE', 'ACCEPTE', 'REFUSE', 'EN_COURS', 'COMPLETE'));

-- Verify the new constraint
SELECT 
    tc.constraint_name, 
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name = 'paneliste_campagne'
AND tc.constraint_type = 'CHECK';

-- Show current records in paneliste_campagne table
SELECT id, paneliste_id, campagne_id, statut, date_invitation, date_request, date_reponse 
FROM paneliste_campagne 
ORDER BY id;