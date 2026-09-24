-- =====================================================================
-- V1__init.sql
-- Script inicial de criacao do schema, baseado no DER fornecido.
-- Compativel com MySQL 8.x
--
-- DECISOES ASSUMIDAS:
-- 1) Tabela "configuracao" nao tinha atributos definidos no DER.
--    Foram criados campos genericos (taxa_servico, horario_abertura,
--    horario_fechamento) como placeholder. AJUSTAR conforme necessidade real.
-- 2) Removida a FK direta "produto.id_categoria_produto": a relacao
--    Produto <-> Categoria passa a ser feita somente pela tabela
--    associativa "categoria_produto" (evita redundancia N:N).
-- 3) Removida a FK direta "funcionario.id_cargo": o cargo do funcionario
--    e controlado apenas pela tabela associativa "cargo_funcionario"
--    (permite historico / multiplos cargos, com campo "status").
-- 4) Tabelas associativas (N:N) usam CHAVE PRIMARIA COMPOSTA
--    (as duas FKs), sem coluna "id" propria.
-- 5) Tipos de dados nao especificados no DER foram definidos com
--    convencoes padrao (DECIMAL para valores monetarios, DATETIME
--    para datas/horas, VARCHAR(255) para senha assumindo hash).
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- LOCALIZACAO
-- =====================================================================

CREATE TABLE estado (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cidade (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    id_estado   INT NOT NULL,
    CONSTRAINT fk_cidade_estado
        FOREIGN KEY (id_estado) REFERENCES estado (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE endereco (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    logradouro  VARCHAR(150) NOT NULL,
    numero      VARCHAR(20)  NOT NULL,
    referencia  VARCHAR(150),
    id_cidade   INT NOT NULL,
    CONSTRAINT fk_endereco_cidade
        FOREIGN KEY (id_cidade) REFERENCES cidade (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- PESSOAS / PERFIL
-- =====================================================================

CREATE TABLE perfil (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    cpf         VARCHAR(14)  NOT NULL,
    telefone    VARCHAR(20),
    id_endereco INT NOT NULL,
    CONSTRAINT uq_perfil_cpf UNIQUE (cpf),
    CONSTRAINT fk_perfil_endereco
        FOREIGN KEY (id_endereco) REFERENCES endereco (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cliente (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_perfil   INT NOT NULL,
    CONSTRAINT uq_cliente_perfil UNIQUE (id_perfil),
    CONSTRAINT fk_cliente_perfil
        FOREIGN KEY (id_perfil) REFERENCES perfil (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- CONFIGURACAO / RESTAURANTE
-- =====================================================================

CREATE TABLE configuracao (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    taxa_servico        DECIMAL(5,2)  DEFAULT 0.00,
    horario_abertura    TIME,
    horario_fechamento  TIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE restaurante (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(150) NOT NULL,
    cnpj             VARCHAR(18)  NOT NULL,
    id_endereco      INT NOT NULL,
    telefone         VARCHAR(20),
    id_configuracao  INT NOT NULL,
    CONSTRAINT uq_restaurante_cnpj UNIQUE (cnpj),
    CONSTRAINT uq_restaurante_configuracao UNIQUE (id_configuracao),
    CONSTRAINT fk_restaurante_endereco
        FOREIGN KEY (id_endereco) REFERENCES endereco (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_restaurante_configuracao
        FOREIGN KEY (id_configuracao) REFERENCES configuracao (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comanda (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    identificacao   VARCHAR(50) NOT NULL,
    status          VARCHAR(30) NOT NULL,
    id_restaurante  INT NOT NULL,
    CONSTRAINT fk_comanda_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurante (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mesa (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    identificacao   VARCHAR(50) NOT NULL,
    area            VARCHAR(50),
    status          VARCHAR(30) NOT NULL,
    id_restaurante  INT NOT NULL,
    CONSTRAINT fk_mesa_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurante (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- ESTRUTURA ORGANIZACIONAL (SETOR / CARGO / PERMISSAO)
-- =====================================================================

CREATE TABLE setor (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    id_restaurante  INT NOT NULL,
    CONSTRAINT fk_setor_restaurante
        FOREIGN KEY (id_restaurante) REFERENCES restaurante (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cargo (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    id_setor    INT NOT NULL,
    CONSTRAINT fk_cargo_setor
        FOREIGN KEY (id_setor) REFERENCES setor (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE funcionario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_perfil   INT NOT NULL,
    login       VARCHAR(50)  NOT NULL,
    senha       VARCHAR(255) NOT NULL,
    status      VARCHAR(30)  NOT NULL,
    CONSTRAINT uq_funcionario_perfil UNIQUE (id_perfil),
    CONSTRAINT uq_funcionario_login UNIQUE (login),
    CONSTRAINT fk_funcionario_perfil
        FOREIGN KEY (id_perfil) REFERENCES perfil (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cargo_funcionario (
    id_cargo        INT NOT NULL,
    id_funcionario  INT NOT NULL,
    status          VARCHAR(30) NOT NULL,
    PRIMARY KEY (id_cargo, id_funcionario),
    CONSTRAINT fk_cargofunc_cargo
        FOREIGN KEY (id_cargo) REFERENCES cargo (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_cargofunc_funcionario
        FOREIGN KEY (id_funcionario) REFERENCES funcionario (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE permissao (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    descricao   VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE permissao_cargo (
    id_permissao    INT NOT NULL,
    id_cargo        INT NOT NULL,
    situacao        VARCHAR(30) NOT NULL,
    PRIMARY KEY (id_permissao, id_cargo),
    CONSTRAINT fk_permcargo_permissao
        FOREIGN KEY (id_permissao) REFERENCES permissao (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_permcargo_cargo
        FOREIGN KEY (id_cargo) REFERENCES cargo (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- PRODUTO / CATEGORIA / MARCA / ESTOQUE / FORNECEDOR
-- =====================================================================

CREATE TABLE categoria (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE marca_produto (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE produto (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nome                VARCHAR(150) NOT NULL,
    valor_produto       DECIMAL(10,2) NOT NULL,
    id_marca_produto    INT NOT NULL,
    CONSTRAINT fk_produto_marca
        FOREIGN KEY (id_marca_produto) REFERENCES marca_produto (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE categoria_produto (
    id_categoria    INT NOT NULL,
    id_produto      INT NOT NULL,
    PRIMARY KEY (id_categoria, id_produto),
    CONSTRAINT fk_categoriaproduto_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_categoriaproduto_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fornecedor (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    cnpj        VARCHAR(18)  NOT NULL,
    telefone    VARCHAR(20),
    email       VARCHAR(150),
    id_endereco INT NOT NULL,
    CONSTRAINT uq_fornecedor_cnpj UNIQUE (cnpj),
    CONSTRAINT fk_fornecedor_endereco
        FOREIGN KEY (id_endereco) REFERENCES endereco (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE produto_fornecedor (
    id_produto      INT NOT NULL,
    id_fornecedor   INT NOT NULL,
    PRIMARY KEY (id_produto, id_fornecedor),
    CONSTRAINT fk_produtofornecedor_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_produtofornecedor_fornecedor
        FOREIGN KEY (id_fornecedor) REFERENCES fornecedor (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE estoque (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_produto  INT NOT NULL,
    quantidade  INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_estoque_produto UNIQUE (id_produto),
    CONSTRAINT fk_estoque_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- PEDIDO
-- =====================================================================

CREATE TABLE pedido (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    id_comanda      INT NOT NULL,
    id_mesa         INT NOT NULL,
    id_cliente      INT NOT NULL,
    id_funcionario  INT NOT NULL,
    valor_total     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status          VARCHAR(30) NOT NULL,
    data_criacao    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_edicao     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_comanda
        FOREIGN KEY (id_comanda) REFERENCES comanda (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_mesa
        FOREIGN KEY (id_mesa) REFERENCES mesa (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_funcionario
        FOREIGN KEY (id_funcionario) REFERENCES funcionario (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pedido_item (
    id_pedido       INT NOT NULL,
    id_produto      INT NOT NULL,
    quantidade      INT NOT NULL DEFAULT 1,
    valor_pedido    DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_pedido, id_produto),
    CONSTRAINT fk_pedidoitem_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_pedidoitem_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
