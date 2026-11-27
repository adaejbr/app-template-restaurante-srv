package com.app.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.datasource.url=${MYSQL_TEST_URL:jdbc:mysql://localhost:3307/infra_test}",
    "spring.datasource.username=${MYSQL_TEST_USER:infra_test}",
    "spring.datasource.password=${MYSQL_TEST_PASSWORD:infra_test}",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "jwt.secret=YXBsaWNhY2FvLXRlc3RlLWNoYXZlLWp3dC0zMi1ieXRlcw=="
})
@ActiveProfiles("dev")
@TestInstance(Lifecycle.PER_CLASS)
class SearchIndexesMySqlIT {

    private static final String MARKER = "BUSCA02_";
    private static final int SEED_SIZE = 10000;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer restauranteId;
    private Integer funcionarioId;
    private Integer estadoId;
    private Integer rareComandaId;
    private Integer rareEnderecoId;
    private Integer rareProdutoId;

    @BeforeAll
    void seed() {
        cleanup();
        restauranteId = jdbc.queryForObject(
                "SELECT id FROM restaurante ORDER BY id LIMIT 1", Integer.class);
        funcionarioId = jdbc.queryForObject(
                "SELECT id FROM funcionario ORDER BY id LIMIT 1", Integer.class);

        seedLocalizacao();
        seedPerfisClientes();
        seedSetorCargos();
        seedMesas();
        seedComandas();
        seedProdutosECategorias();
        seedPedidos();
        analyzeTables();
    }

    @AfterAll
    void cleanupAfter() {
        cleanup();
    }

    private void cleanup() {
        // Ordem FK: dependentes antes dos referenciados. Apenas linhas BUSCA02_.
        jdbc.update("DELETE pi FROM pedido_item pi "
                + "JOIN pedido p ON p.id = pi.id_pedido "
                + "JOIN comanda c ON c.id = p.id_comanda "
                + "WHERE c.identificacao LIKE ?", MARKER + "%");
        jdbc.update("DELETE p FROM pedido p "
                + "JOIN comanda c ON c.id = p.id_comanda "
                + "WHERE c.identificacao LIKE ?", MARKER + "%");

        jdbc.update("DELETE cp FROM categoria_produto cp "
                + "JOIN produto pr ON pr.id = cp.id_produto "
                + "WHERE pr.nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE pf FROM produto_fornecedor pf "
                + "JOIN produto pr ON pr.id = pf.id_produto "
                + "WHERE pr.nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE e FROM estoque e "
                + "JOIN produto pr ON pr.id = e.id_produto "
                + "WHERE pr.nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM produto WHERE nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM categoria WHERE nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM marca_produto WHERE nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM fornecedor WHERE nome LIKE ?", MARKER + "%");

        jdbc.update("DELETE FROM comanda WHERE identificacao LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM mesa WHERE identificacao LIKE ?", MARKER + "%");

        jdbc.update("DELETE cf FROM cargo_funcionario cf "
                + "JOIN cargo c ON c.id = cf.id_cargo "
                + "WHERE c.nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE pc FROM permissao_cargo pc "
                + "JOIN cargo c ON c.id = pc.id_cargo "
                + "WHERE c.nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM cargo WHERE nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM setor WHERE nome LIKE ?", MARKER + "%");

        jdbc.update("DELETE cl FROM cliente cl "
                + "JOIN perfil p ON p.id = cl.id_perfil "
                + "WHERE p.nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM perfil WHERE nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM endereco WHERE referencia LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM cidade WHERE nome LIKE ?", MARKER + "%");
        jdbc.update("DELETE FROM estado WHERE nome LIKE ?", MARKER + "%");
    }
    private void seedLocalizacao() {
        jdbc.update("INSERT INTO estado (nome) VALUES (?)", MARKER + "ESTADO");
        estadoId = jdbc.queryForObject(
                "SELECT id FROM estado WHERE nome = ?", Integer.class, MARKER + "ESTADO");

        jdbc.update(
                "INSERT INTO cidade (nome, id_estado) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT CONCAT(?, LPAD(n, 5, '0')), ? FROM seq",
                SEED_SIZE, MARKER + "CIDADE_", estadoId);

        jdbc.update(
                "INSERT INTO endereco (logradouro, numero, referencia, id_cidade) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT CONCAT(?, LPAD(seq.n, 5, '0')), CAST(seq.n AS CHAR), "
                + "  CONCAT(?, LPAD(seq.n, 5, '0')), c.id "
                + "FROM seq "
                + "JOIN (SELECT id, CAST(RIGHT(nome, 5) AS UNSIGNED) AS n "
                + "  FROM cidade WHERE nome LIKE ?) c ON c.n = seq.n",
                SEED_SIZE, MARKER + "RUA_", MARKER + "REF_", MARKER + "CIDADE_%");

        rareEnderecoId = jdbc.queryForObject(
                "SELECT id FROM endereco WHERE referencia = ?",
                Integer.class, MARKER + "REF_00001");
    }

    private void seedPerfisClientes() {
        jdbc.update(
                "INSERT INTO perfil (nome, cpf, telefone, id_endereco) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT CONCAT(?, LPAD(seq.n, 5, '0')), CONCAT('B02', LPAD(seq.n, 11, '0')), "
                + "  CONCAT('119', LPAD(seq.n, 8, '0')), e.id "
                + "FROM seq "
                + "JOIN (SELECT id, CAST(RIGHT(referencia, 5) AS UNSIGNED) AS n "
                + "  FROM endereco WHERE referencia LIKE ?) e ON e.n = seq.n",
                SEED_SIZE, MARKER + "PERFIL_", MARKER + "REF_%");

        jdbc.update(
                "INSERT INTO cliente (id_perfil) "
                + "SELECT p.id FROM perfil p WHERE p.nome LIKE ?",
                MARKER + "PERFIL_%");
    }

    private void seedSetorCargos() {
        jdbc.update(
                "INSERT INTO setor (nome, id_restaurante) VALUES (?, ?)",
                MARKER + "SETOR", restauranteId);

        Integer setorId = jdbc.queryForObject(
                "SELECT id FROM setor WHERE nome = ?", Integer.class, MARKER + "SETOR");

        jdbc.update(
                "INSERT INTO cargo (nome, id_setor) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT CONCAT(?, LPAD(n, 5, '0')), ? FROM seq",
                SEED_SIZE, MARKER + "CARGO_", setorId);
    }

    private void seedMesas() {
        jdbc.update(
                "INSERT INTO mesa (identificacao, area, status, id_restaurante) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT CONCAT(?, LPAD(n, 5, '0')), "
                + "  CASE WHEN n = 1 THEN ? ELSE ? END, "
                + "  CASE WHEN n = 1 THEN 'LIVRE' ELSE 'OCUPADA' END, "
                + "  ? "
                + "FROM seq",
                SEED_SIZE, MARKER + "MESA_", MARKER + "A", MARKER + "B", restauranteId);
    }

    private void seedComandas() {
        jdbc.update(
                "INSERT INTO comanda (identificacao, status, id_restaurante) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT CONCAT(?, LPAD(n, 5, '0')), "
                + "  CASE WHEN n = 1 THEN ? ELSE ? END, "
                + "  ? "
                + "FROM seq",
                SEED_SIZE, MARKER + "COMANDA_", MARKER + "ABERTA", MARKER + "FECHADA",
                restauranteId);

        rareComandaId = jdbc.queryForObject(
                "SELECT id FROM comanda WHERE identificacao = ?",
                Integer.class, MARKER + "COMANDA_00001");
    }

    private void seedProdutosECategorias() {
        jdbc.update("INSERT INTO marca_produto (nome) VALUES (?)", MARKER + "MARCA");
        Integer marcaId = jdbc.queryForObject(
                "SELECT id FROM marca_produto WHERE nome = ?", Integer.class, MARKER + "MARCA");

        jdbc.update(
                "INSERT INTO produto (nome, valor_produto, id_marca_produto) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT CONCAT(?, LPAD(n, 5, '0')), 10.00, ? FROM seq",
                SEED_SIZE, MARKER + "PRODUTO_", marcaId);

        jdbc.update("INSERT INTO categoria (nome) VALUES (?)", MARKER + "CATEGORIA");
        Integer categoriaId = jdbc.queryForObject(
                "SELECT id FROM categoria WHERE nome = ?", Integer.class, MARKER + "CATEGORIA");

        jdbc.update(
                "INSERT INTO categoria_produto (id_categoria, id_produto) "
                + "SELECT ?, p.id FROM produto p WHERE p.nome LIKE ?",
                categoriaId, MARKER + "PRODUTO_%");

        rareProdutoId = jdbc.queryForObject(
                "SELECT id FROM produto WHERE nome = ?",
                Integer.class, MARKER + "PRODUTO_00001");
    }

    private void seedPedidos() {
        // Reusa o funcionario seedado; clientes/mesas/comandas BUSCA02_.
        jdbc.update(
                "INSERT INTO pedido (id_comanda, id_mesa, id_cliente, id_funcionario, "
                + "valor_total, status, data_criacao, data_edicao) "
                + "WITH RECURSIVE seq AS ("
                + "  SELECT 1 AS n "
                + "  UNION ALL "
                + "  SELECT n + 1 FROM seq WHERE n < ?"
                + ") "
                + "SELECT co.id, m.id, cl.id, ?, 1.00, "
                + "  CASE WHEN seq.n = 1 THEN ? ELSE ? END, NOW(), NOW() "
                + "FROM seq "
                + "JOIN (SELECT id, CAST(RIGHT(identificacao, 5) AS UNSIGNED) AS n "
                + "  FROM comanda WHERE identificacao LIKE ?) co ON co.n = seq.n "
                + "JOIN (SELECT id, CAST(RIGHT(identificacao, 5) AS UNSIGNED) AS n "
                + "  FROM mesa WHERE identificacao LIKE ?) m ON m.n = seq.n "
                + "JOIN (SELECT c.id, CAST(RIGHT(p.nome, 5) AS UNSIGNED) AS n "
                + "  FROM cliente c JOIN perfil p ON p.id = c.id_perfil "
                + "  WHERE p.nome LIKE ?) cl ON cl.n = seq.n",
                SEED_SIZE, funcionarioId, MARKER + "STATUS_RARO", MARKER + "STATUS",
                MARKER + "COMANDA_%", MARKER + "MESA_%", MARKER + "PERFIL_%");

        jdbc.update(
                "INSERT INTO pedido_item (id_pedido, id_produto, quantidade, valor_pedido) "
                + "SELECT p.id, ?, 1, 1.00 FROM pedido p "
                + "JOIN comanda c ON c.id = p.id_comanda "
                + "WHERE c.identificacao LIKE ?",
                rareProdutoId, MARKER + "COMANDA_%");
    }

    private void analyzeTables() {
        for (String table : List.of("mesa", "comanda", "pedido", "perfil", "endereco",
                "cidade", "estado", "categoria_produto", "produto", "categoria",
                "marca_produto", "setor", "cargo", "fornecedor", "permissao_cargo",
                "produto_fornecedor", "pedido_item", "cliente")) {
            jdbc.execute("ANALYZE TABLE " + table);
        }
    }

    @Test
    void v12IndexesExist() {
        Set<String> expected = Set.of(
                "idx_mesa_restaurante",
                "idx_comanda_restaurante",
                "idx_setor_restaurante",
                "idx_cargo_setor",
                "idx_perfil_endereco",
                "idx_endereco_cidade",
                "idx_cidade_estado",
                "idx_fornecedor_endereco",
                "idx_produto_marca_produto",
                "idx_pedido_comanda",
                "idx_pedido_mesa",
                "idx_pedido_cliente",
                "idx_pedido_funcionario",
                "idx_categoria_produto_produto",
                "idx_produto_fornecedor_fornecedor",
                "idx_permissao_cargo_cargo",
                "idx_pedido_item_produto",
                "idx_mesa_status_area",
                "idx_comanda_status",
                "idx_pedido_status",
                "idx_produto_nome",
                "idx_fornecedor_nome",
                "idx_categoria_nome",
                "idx_marca_produto_nome",
                "idx_setor_nome",
                "idx_cargo_nome",
                "idx_mesa_identificacao",
                "idx_comanda_identificacao",
                "idx_permissao_nome"
        );

        for (String indexName : expected) {
            assertTrue(indexExists(indexName),
                    "Indice V12 ausente: " + indexName);
        }
    }

    @Test
    void explainUsesSelectiveIndexes() {
        // LIKE '%termo%' nao usa B-tree; nao asserir uso de indice nesse caso.

        assertIndexUsed(
                "SELECT id FROM mesa WHERE status = 'LIVRE' AND area = '" + MARKER + "A'",
                "idx_mesa_status_area");

        assertIndexUsed(
                "SELECT id FROM comanda WHERE status = '" + MARKER + "ABERTA'",
                "idx_comanda_status");

        assertIndexUsed(
                "SELECT id FROM pedido WHERE id_comanda = " + rareComandaId,
                "idx_pedido_comanda");

        assertIndexUsed(
                "SELECT id FROM pedido WHERE status = '" + MARKER + "STATUS_RARO'",
                "idx_pedido_status");

        assertIndexUsed(
                "SELECT id FROM produto ORDER BY nome LIMIT 50",
                "idx_produto_nome");

        assertIndexUsed(
                "SELECT id FROM perfil WHERE id_endereco = " + rareEnderecoId,
                "idx_perfil_endereco");

        assertIndexUsed(
                "SELECT id_produto FROM categoria_produto WHERE id_produto = " + rareProdutoId,
                "idx_categoria_produto_produto");
    }

    private boolean indexExists(String indexName) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME = ?",
                indexName);
        return !rows.isEmpty();
    }

    private void assertIndexUsed(String query, String expectedIndex) {
        List<Map<String, Object>> explain = jdbc.queryForList("EXPLAIN " + query);
        assertFalse(explain.isEmpty(), "EXPLAIN sem linhas para: " + query);

        Set<String> usedKeys = new HashSet<>();
        for (Map<String, Object> row : explain) {
            Object key = row.get("key");
            if (key == null) {
                key = row.get("KEY");
            }
            if (key != null) {
                usedKeys.add(String.valueOf(key));
            }
        }

        assertTrue(usedKeys.contains(expectedIndex),
                "Esperava indice " + expectedIndex + " em " + query + ", keys=" + usedKeys
                + ", explain=" + explain);
    }

}
