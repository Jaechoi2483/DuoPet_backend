-- SQL script to modify the SHELTER_ID constraint to allow NULL values
-- This is needed because API-sourced animals don't have a shelter entity reference

-- Make SHELTER_ID nullable in SHELTER_ANIMALS table
ALTER TABLE DUOPET.SHELTER_ANIMALS MODIFY SHELTER_ID NULL;

-- Verify the change
-- SELECT COLUMN_NAME, NULLABLE 
-- FROM USER_TAB_COLUMNS 
-- WHERE TABLE_NAME = 'SHELTER_ANIMALS' 
-- AND COLUMN_NAME = 'SHELTER_ID';