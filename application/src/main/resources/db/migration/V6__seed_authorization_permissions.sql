-- Chaves de autorização iniciais, organizadas por recurso e ação.
-- V2 já inclui permissões gerais; estas complementam o conjunto legado.
INSERT INTO permissao (nome, descricao)
SELECT seed.nome, seed.descricao
FROM (
    SELECT 'FUNCIONARIOS_VISUALIZAR' AS nome, 'Permite visualizar funcionarios' AS descricao
    UNION ALL SELECT 'FUNCIONARIOS_CRIAR', 'Permite cadastrar funcionarios'
    UNION ALL SELECT 'FUNCIONARIOS_EDITAR', 'Permite editar funcionarios'
    UNION ALL SELECT 'FUNCIONARIOS_EXCLUIR', 'Permite remover funcionarios'
    UNION ALL SELECT 'CARGOS_VISUALIZAR', 'Permite visualizar cargos e permissoes'
    UNION ALL SELECT 'CARGOS_CRIAR', 'Permite cadastrar cargos'
    UNION ALL SELECT 'CARGOS_EDITAR', 'Permite editar cargos e permissoes'
    UNION ALL SELECT 'CARGOS_EXCLUIR', 'Permite remover cargos'
    UNION ALL SELECT 'PRODUTOS_VISUALIZAR', 'Permite visualizar produtos e categorias'
    UNION ALL SELECT 'PRODUTOS_GERENCIAR', 'Permite cadastrar, editar e remover produtos e categorias'
    UNION ALL SELECT 'ESTOQUE_VISUALIZAR', 'Permite consultar quantidades em estoque'
    UNION ALL SELECT 'ESTOQUE_GERENCIAR', 'Permite ajustar quantidades em estoque'
    UNION ALL SELECT 'PEDIDOS_VISUALIZAR', 'Permite visualizar pedidos'
    UNION ALL SELECT 'PEDIDOS_CRIAR', 'Permite criar pedidos'
    UNION ALL SELECT 'PEDIDOS_EDITAR', 'Permite editar pedidos'
    UNION ALL SELECT 'PEDIDOS_FINALIZAR', 'Permite finalizar pedidos'
    UNION ALL SELECT 'RELATORIOS_VISUALIZAR', 'Permite acessar relatorios gerenciais'
) AS seed
LEFT JOIN permissao existente ON existente.nome = seed.nome
WHERE existente.id IS NULL;
