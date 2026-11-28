-- Canonicalize legacy values before enforcing uniqueness through normalized application writes.
-- Existing canonical collisions deliberately fail the migration instead of discarding restaurant data.
UPDATE restaurante
SET cnpj = UPPER(REPLACE(REPLACE(REPLACE(TRIM(cnpj), '.', ''), '/', ''), '-', ''));
