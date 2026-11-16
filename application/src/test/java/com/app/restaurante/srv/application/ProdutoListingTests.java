package com.app.restaurante.srv.application;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProdutoListingTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("INSERT INTO categoria (id, nome) VALUES (901, 'Pizzas'), (902, 'Sucos')");
        jdbc.update("INSERT INTO marca_produto (id, nome) VALUES (901, 'Casa')");
        jdbc.update("""
                INSERT INTO produto (id, nome, valor_produto, id_marca_produto)
                VALUES (901, 'Pizza Margherita', 45.00, 901),
                       (902, 'Pizza Calabresa', 49.90, 901),
                       (903, 'Suco de Laranja', 12.50, 901)
                """);
        jdbc.update("""
                INSERT INTO categoria_produto (id_categoria, id_produto)
                VALUES (901, 901),
                       (901, 902),
                       (902, 903)
                """);
    }

    @Test
    void listsProductsWithQAndCategoriaIdAndPaginationAndSort() throws Exception {
        mvc.perform(get("/api/produtos")
                        .with(user("teste"))
                        .param("q", "pizza")
                        .param("categoriaId", "901")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "nome,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("Pizza Margherita"))
                .andExpect(jsonPath("$.content[1].nome").value("Pizza Calabresa"));
    }

    @Test
    void filtersByCategoriaIdOnly() throws Exception {
        mvc.perform(get("/api/produtos")
                        .with(user("teste"))
                        .param("categoriaId", "902"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(903))
                .andExpect(jsonPath("$.content[0].nome").value("Suco de Laranja"));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/produtos"))
                .andExpect(status().isUnauthorized());
    }
}
