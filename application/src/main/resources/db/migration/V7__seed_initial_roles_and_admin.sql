-- Renomeia o cargo legado para o nome inicial definido para o RBAC.
UPDATE cargo
SET nome = 'Gerente'
WHERE nome = 'GERENTE';

-- O restaurante e o setor administrativo sao criados pelo seed V2.
INSERT INTO cargo (nome, id_setor)
SELECT 'Garçom', s.id
FROM setor s
WHERE s.nome = 'Administrativo'
  AND NOT EXISTS (
      SELECT 1
      FROM cargo existente
      WHERE existente.nome = 'Garçom'
        AND existente.id_setor = s.id
  );

-- O gerente recebe todas as chaves de autorização, inclusive as legadas.
INSERT INTO permissao_cargo (id_permissao, id_cargo, situacao)
SELECT p.id, c.id, 'ATIVA'
FROM permissao p
CROSS JOIN cargo c
WHERE c.nome = 'Gerente'
  AND NOT EXISTS (
      SELECT 1
      FROM permissao_cargo pc
      WHERE pc.id_permissao = p.id
        AND pc.id_cargo = c.id
  );

-- O garcom pode consultar produtos e operar pedidos, sem permissao administrativa.
INSERT INTO permissao_cargo (id_permissao, id_cargo, situacao)
SELECT p.id, c.id, 'ATIVA'
FROM permissao p
CROSS JOIN cargo c
WHERE c.nome = 'Garçom'
  AND p.nome IN (
      'PRODUTOS_VISUALIZAR',
      'PEDIDOS_VISUALIZAR',
      'PEDIDOS_CRIAR',
      'PEDIDOS_EDITAR',
      'PEDIDOS_FINALIZAR'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM permissao_cargo pc
      WHERE pc.id_permissao = p.id
        AND pc.id_cargo = c.id
  );

-- Migra a senha legada sem sobrescrever senhas já alteradas ou hasheadas.
UPDATE funcionario
SET senha = '$2a$10$7Xc4OVr4Fi7dNchnWWnSSOv1z6lOZzMgAkhokGcwClHcR5DzYLOOq'
WHERE login = 'gerente@restaurante.com'
  AND senha = 'admin123';
