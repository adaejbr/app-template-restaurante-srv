-- Chaves de autorizacao do restaurante (REST-04).
-- O padrao do projeto e CHAVE:ACAO, sem prefixo ROLE_.
INSERT INTO permissao (chave, nome, descricao)
SELECT seed.chave, seed.nome, seed.descricao
FROM (
    SELECT 'RESTAURANTE_VISUALIZAR' AS chave, 'Restaurante: visualizar' AS nome,
           'Permite visualizar o restaurante' AS descricao
    UNION ALL SELECT 'RESTAURANTE_EDITAR', 'Restaurante: editar', 'Permite editar o restaurante'
) AS seed
LEFT JOIN permissao existente ON existente.chave = seed.chave
WHERE existente.id IS NULL;

-- O cargo Gerente recebe as novas chaves (mesmo padrao da V9).
INSERT INTO permissao_cargo (id_permissao, id_cargo, situacao)
SELECT p.id, c.id, 'ATIVA'
FROM permissao p
CROSS JOIN cargo c
WHERE c.nome = 'Gerente'
  AND p.chave IN ('RESTAURANTE_VISUALIZAR', 'RESTAURANTE_EDITAR')
  AND NOT EXISTS (
      SELECT 1
      FROM permissao_cargo pc
      WHERE pc.id_permissao = p.id
        AND pc.id_cargo = c.id
  );
