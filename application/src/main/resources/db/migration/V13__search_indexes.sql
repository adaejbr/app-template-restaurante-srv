-- BUSCA-02 (#61): indices para as listagens com filtros e joins.
-- LIKE '%termo%' nao usa B-tree; os indices de nome cobrem ORDER BY e prefixo.
-- FULLTEXT foi avaliado e adiado: busca atual e por substring, e MATCH mudaria
-- a semantica e quebraria os testes H2.

-- FK usadas nos JOINs das listagens (InnoDB nao indexa FK automaticamente).
CREATE INDEX idx_mesa_restaurante ON mesa (id_restaurante);
CREATE INDEX idx_comanda_restaurante ON comanda (id_restaurante);
CREATE INDEX idx_setor_restaurante ON setor (id_restaurante);
CREATE INDEX idx_cargo_setor ON cargo (id_setor);
CREATE INDEX idx_perfil_endereco ON perfil (id_endereco);
CREATE INDEX idx_endereco_cidade ON endereco (id_cidade);
CREATE INDEX idx_cidade_estado ON cidade (id_estado);
CREATE INDEX idx_fornecedor_endereco ON fornecedor (id_endereco);
CREATE INDEX idx_produto_marca_produto ON produto (id_marca_produto);
CREATE INDEX idx_pedido_comanda ON pedido (id_comanda);
CREATE INDEX idx_pedido_mesa ON pedido (id_mesa);
CREATE INDEX idx_pedido_cliente ON pedido (id_cliente);
CREATE INDEX idx_pedido_funcionario ON pedido (id_funcionario);

-- Segunda coluna das PKs compostas (a PK cobre apenas a primeira).
CREATE INDEX idx_categoria_produto_produto ON categoria_produto (id_produto);
CREATE INDEX idx_produto_fornecedor_fornecedor ON produto_fornecedor (id_fornecedor);
CREATE INDEX idx_permissao_cargo_cargo ON permissao_cargo (id_cargo);
CREATE INDEX idx_pedido_item_produto ON pedido_item (id_produto);

-- Filtros de igualdade usados nas Specifications.
CREATE INDEX idx_mesa_status_area ON mesa (status, area);
CREATE INDEX idx_comanda_status ON comanda (status);
CREATE INDEX idx_pedido_status ON pedido (status);

-- Colunas de texto para ORDER BY e busca por prefixo.
CREATE INDEX idx_produto_nome ON produto (nome);
CREATE INDEX idx_fornecedor_nome ON fornecedor (nome);
CREATE INDEX idx_categoria_nome ON categoria (nome);
CREATE INDEX idx_marca_produto_nome ON marca_produto (nome);
CREATE INDEX idx_setor_nome ON setor (nome);
CREATE INDEX idx_cargo_nome ON cargo (nome);
CREATE INDEX idx_mesa_identificacao ON mesa (identificacao);
CREATE INDEX idx_comanda_identificacao ON comanda (identificacao);
CREATE INDEX idx_permissao_nome ON permissao (nome);
