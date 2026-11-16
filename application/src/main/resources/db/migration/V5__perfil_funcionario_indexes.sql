-- FUNC-01: as tabelas e constraints de unicidade/FK ja foram criadas na V1.
-- Preserva os dados existentes e o vinculo N:N funcionario/cargo.
CREATE INDEX idx_perfil_nome ON perfil (nome);
CREATE INDEX idx_funcionario_status ON funcionario (status);
CREATE INDEX idx_cargofunc_funcionario_status ON cargo_funcionario (id_funcionario, status);
