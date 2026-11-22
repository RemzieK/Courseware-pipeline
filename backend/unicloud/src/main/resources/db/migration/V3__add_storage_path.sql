-- UC-29: Add storage_path to material_versions for physical storage tracking
ALTER TABLE material_versions ADD COLUMN storage_path VARCHAR(1000);
