ALTER TABLE restaurante
ADD COLUMN singleton_key TINYINT
GENERATED ALWAYS AS (1) STORED,
ADD CONSTRAINT uq_restaurante_singleton UNIQUE (singleton_key);