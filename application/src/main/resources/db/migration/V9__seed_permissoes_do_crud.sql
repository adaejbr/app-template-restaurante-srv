-- Chaves de autorizacao do CRUD de permissoes (PERM-02).
-- A V8 tornou "chave" NOT NULL, entao o seed informa chave e rotulo.
INSERT INTO permissao (chave, nome, descricao)
SELECT seed.chave, seed.nome, seed.descricao
FROM (
    SELECT 'PERMISSOES_VISUALIZAR' AS chave, 'Permissoes: visualizar' AS nome,
           'Permite visualizar permissoes' AS descricao
    UNION ALL SELECT 'PERMISSOES_CRIAR', 'Permissoes: criar', 'Permite cadastrar permissoes'
    UNION ALL SELECT 'PERMISSOES_EDITAR', 'Permissoes: editar', 'Permite editar permissoes'
    UNION ALL SELECT 'PERMISSOES_EXCLUIR', 'Permissoes: excluir', 'Permite remover permissoes'
) AS seed
LEFT JOIN permissao existente ON existente.chave = seed.chave
WHERE existente.id IS NULL;

-- O cargo Gerente recebe as novas chaves (mesmo padrao da V7).
INSERT INTO permissao_cargo (id_permissao, id_cargo, situacao)
SELECT p.id, c.id, 'ATIVA'
FROM permissao p
CROSS JOIN cargo c
WHERE c.nome = 'Gerente'
  AND p.chave IN ('PERMISSOES_VISUALIZAR', 'PERMISSOES_CRIAR', 'PERMISSOES_EDITAR', 'PERMISSOES_EXCLUIR')
  AND NOT EXISTS (
      SELECT 1
      FROM permissao_cargo pc
      WHERE pc.id_permissao = p.id
        AND pc.id_cargo = c.id
  );
