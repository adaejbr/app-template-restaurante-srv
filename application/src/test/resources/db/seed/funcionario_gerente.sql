-- =====================================================================
-- Fixture do DataInitializerTests.
--
-- Espelha a parte de V2__seed_data.sql que o teste precisa (cargo e
-- funcionario com senha em texto plano), sem as permissoes: no teste o
-- schema vem da entidade, portanto "permissao.chave" ja existe e e NOT
-- NULL, e o INSERT antigo da V2 nao serve. A V2 continua intocada porque
-- no MySQL ela roda antes da V8, quando a coluna ainda nao existe.
-- =====================================================================

INSERT INTO estado (nome) VALUES ('Parana');

INSERT INTO cidade (nome, id_estado)
VALUES ('Medianeira', (SELECT id FROM estado WHERE nome = 'Parana' LIMIT 1));

INSERT INTO endereco (logradouro, numero, referencia, id_cidade)
VALUES ('Endereco Padrao', 'S/N', NULL,
        (SELECT id FROM cidade WHERE nome = 'Medianeira' LIMIT 1));

INSERT INTO configuracao (taxa_servico, horario_abertura, horario_fechamento)
VALUES (10.00, '11:00:00', '23:00:00');

INSERT INTO restaurante (nome, cnpj, id_endereco, telefone, id_configuracao)
VALUES (
    'Restaurante Padrao',
    '00000000000000',
    (SELECT id FROM endereco WHERE logradouro = 'Endereco Padrao' LIMIT 1),
    NULL,
    (SELECT id FROM configuracao ORDER BY id LIMIT 1)
);

INSERT INTO setor (nome, id_restaurante)
VALUES ('Administrativo',
        (SELECT id FROM restaurante WHERE nome = 'Restaurante Padrao' LIMIT 1));

INSERT INTO cargo (nome, id_setor)
VALUES ('GERENTE',
        (SELECT id FROM setor WHERE nome = 'Administrativo' LIMIT 1));

INSERT INTO perfil (nome, cpf, telefone, id_endereco)
VALUES (
    'Gerente Padrao',
    '00000000000',
    NULL,
    (SELECT id FROM endereco WHERE logradouro = 'Endereco Padrao' LIMIT 1)
);

-- Senha em texto plano de proposito: o DataInitializer hasheia na subida.
INSERT INTO funcionario (id_perfil, login, senha, status)
VALUES (
    (SELECT id FROM perfil WHERE cpf = '00000000000' LIMIT 1),
    'gerente@restaurante.com',
    'admin123',
    'ATIVO'
);

INSERT INTO cargo_funcionario (id_cargo, id_funcionario, status)
VALUES (
    (SELECT id FROM cargo WHERE nome = 'GERENTE' LIMIT 1),
    (SELECT id FROM funcionario WHERE login = 'gerente@restaurante.com' LIMIT 1),
    'ATIVO'
);
