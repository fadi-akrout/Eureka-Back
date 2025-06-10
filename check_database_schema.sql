-- SQL script to check the current database schema for paneliste_campagne table

-- Check if the table exists and its structure
\d paneliste_campagne;

-- Check the column definition for statut
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default,
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'paneliste_campagne' 
AND column_name = 'statut';

-- Check for any check constraints on the table
SELECT 
    tc.constraint_name, 
    tc.constraint_type,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name = 'paneliste_campagne';

-- Check current data in the table
SELECT id, paneliste_id, campagne_id, statut, date_invitation, date_request, date_reponse 
FROM paneliste_campagne 
ORDER BY id;

-- Check if there are any custom enum types
SELECT typname, typtype 
FROM pg_type 
WHERE typtype = 'e';