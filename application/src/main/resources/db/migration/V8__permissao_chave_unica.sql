-- Separa a chave tecnica (codigo) do rotulo humano (nome) e exige unicidade.
ALTER TABLE permissao ADD COLUMN chave VARCHAR(100) NULL AFTER id;

-- Backfill com o nome: as linhas ja semeadas usam a chave como rotulo.
UPDATE permissao SET chave = nome WHERE chave IS NULL;

ALTER TABLE permissao MODIFY chave VARCHAR(100) NOT NULL;

ALTER TABLE permissao ADD CONSTRAINT uq_permissao_chave UNIQUE (chave);
