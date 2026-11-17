-- Controle de status ativo/inativo do cargo (CARGO-02, #66).
ALTER TABLE cargo ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ATIVO';
