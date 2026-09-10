-- =====================================================================
-- V2__seed_data.sql
-- Carga inicial (seed) do banco.
--
-- IMPORTANTE sobre a senha do funcionario:
-- O valor inserido em "senha" e o TEXTO PLANO "admin123". A criptografia
-- (ex: BCrypt) deve ser aplicada pelo Backend no momento do login/auth,
-- OU por uma rotina de inicializacao (CommandLineRunner/ApplicationRunner)
-- que re-hasheia esse valor quando a aplicacao sobe pela primeira vez.
-- Se o backend espera a senha JA hasheada no banco, ajuste este INSERT
-- substituindo 'admin123' pelo hash correspondente antes de rodar.
-- =====================================================================

-- =====================================================================
-- Dependencias minimas de FK (necessarias so para o cargo/funcionario
-- poderem existir - o schema exige restaurante -> setor -> cargo)
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

-- =====================================================================
-- 1) Cargo
-- =====================================================================

INSERT INTO cargo (nome, id_setor)
VALUES ('GERENTE',
        (SELECT id FROM setor WHERE nome = 'Administrativo' LIMIT 1));

-- =====================================================================
-- 2) Funcionario vinculado ao cargo GERENTE
-- =====================================================================

-- Perfil base do funcionario (perfil exige endereco)
INSERT INTO perfil (nome, cpf, telefone, id_endereco)
VALUES (
    'Gerente Padrao',
    '00000000000',
    NULL,
    (SELECT id FROM endereco WHERE logradouro = 'Endereco Padrao' LIMIT 1)
);

INSERT INTO funcionario (id_perfil, login, senha, status)
VALUES (
    (SELECT id FROM perfil WHERE cpf = '00000000000' LIMIT 1),
    'gerente@restaurante.com',
    'admin123',
    'ATIVO'
);

-- Vincula o funcionario ao cargo GERENTE
INSERT INTO cargo_funcionario (id_cargo, id_funcionario, status)
VALUES (
    (SELECT id FROM cargo WHERE nome = 'GERENTE' LIMIT 1),
    (SELECT id FROM funcionario WHERE login = 'gerente@restaurante.com' LIMIT 1),
    'ATIVO'
);

-- =====================================================================
-- 3) Categorias basicas
-- =====================================================================

INSERT INTO categoria (nome) VALUES
    ('Bebidas'),
    ('Comidas'),
    ('Sobremesas');

-- =====================================================================
-- 4) Permissoes iniciais (para validar RBAC)
-- =====================================================================

INSERT INTO permissao (nome, descricao) VALUES
    ('GERENCIAR_FUNCIONARIOS', 'Permite criar, editar e remover funcionarios'),
    ('GERENCIAR_CARGOS',       'Permite criar, editar e remover cargos e permissoes'),
    ('GERENCIAR_PRODUTOS',     'Permite criar, editar e remover produtos e categorias'),
    ('GERENCIAR_ESTOQUE',      'Permite ajustar quantidades em estoque'),
    ('GERENCIAR_PEDIDOS',      'Permite criar, editar e finalizar pedidos'),
    ('VISUALIZAR_RELATORIOS',  'Permite acessar relatorios gerenciais');

-- Vincula TODAS as permissoes iniciais ao cargo GERENTE (acesso total,
-- ja que e o cargo administrativo inicial do sistema)
INSERT INTO permissao_cargo (id_permissao, id_cargo, situacao)
SELECT p.id,
       (SELECT id FROM cargo WHERE nome = 'GERENTE' LIMIT 1),
       'ATIVA'
FROM permissao p
WHERE p.nome IN (
    'GERENCIAR_FUNCIONARIOS',
    'GERENCIAR_CARGOS',
    'GERENCIAR_PRODUTOS',
    'GERENCIAR_ESTOQUE',
    'GERENCIAR_PEDIDOS',
    'VISUALIZAR_RELATORIOS'
);
