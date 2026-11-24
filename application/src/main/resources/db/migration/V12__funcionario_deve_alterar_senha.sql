-- FUNC-05 (#67): marca o funcionario cuja senha foi definida por terceiro
-- (gerente) e que deve criar uma senha pessoal no proximo acesso.
--
-- DEFAULT FALSE preserva os funcionarios existentes, inclusive o gerente do
-- seed: quando o bloqueio por esta flag for implementado, os ambientes de
-- desenvolvimento nao passam a exigir troca no primeiro login.
ALTER TABLE funcionario
    ADD COLUMN deve_alterar_senha BOOLEAN NOT NULL DEFAULT FALSE;
