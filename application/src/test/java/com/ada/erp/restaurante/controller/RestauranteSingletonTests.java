package com.ada.erp.restaurante.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import com.app.restaurante.srv.application.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cobre o criterio de aceite da REST-02 (#74): o restaurante e singleton, entao
 * GET responde 404 ate existir registro e o PUT atualiza sem criar outro.
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class RestauranteSingletonTests {

    private static final int PRINCIPAL = 901;
    private static final int SECUNDARIO = 902;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private int restauranteId;

    @BeforeEach
    void fixtures() {
        List<Integer> ids = jdbc.query("SELECT id FROM restaurante ORDER BY id LIMIT 1",
                (rs, rowNum) -> rs.getInt("id"));
        if (ids.isEmpty()) {
            jdbc.update("INSERT INTO estado(id, nome) VALUES (901, 'Teste')");
            jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (901, 'Teste', 901)");
            jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (901, 'Rua', '1', 901)");
            jdbc.update("INSERT INTO configuracao(id) VALUES (901)");
            jdbc.update("""
                    INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                    VALUES (901, 'Primeiro', '04252011000110', 901, 901)
                    """);
            restauranteId = PRINCIPAL;
        } else {
            restauranteId = ids.get(0);
        }
        jdbc.update("UPDATE restaurante SET nome = ?, cnpj = ? WHERE id = ?",
                "Primeiro", "04252011000110", restauranteId);
    }

    @Test
    void getRetorna404QuandoRestauranteNaoEstaConfigurado() throws Exception {
        jdbc.update("DELETE FROM restaurante");

        mvc.perform(get("/api/restaurante").with(user("teste")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRetorna200ComRestauranteConfigurado() throws Exception {
        mvc.perform(get("/api/restaurante").with(user("teste")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(restauranteId))
                .andExpect(jsonPath("$.nome").value("Primeiro"))
                .andExpect(jsonPath("$.cnpj").value("04252011000110"));
    }

    @Test
    void putAtualizaSemCriarNovoRegistro() throws Exception {
        mvc.perform(put("/api/restaurante").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"nome":"Renomeado","cnpj":"11.222.333/0001-81","telefone":"44332211"}
                                """.formatted(restauranteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(restauranteId))
                .andExpect(jsonPath("$.nome").value("Renomeado"))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"))
                .andExpect(jsonPath("$.telefone").value("44332211"));

        assertEquals(1, contagem("restaurante", null));
        assertEquals("Renomeado", jdbc.queryForObject(
                "SELECT nome FROM restaurante WHERE id = ?", String.class, restauranteId));
        assertEquals("11222333000181", jdbc.queryForObject(
                "SELECT cnpj FROM restaurante WHERE id = ?", String.class, restauranteId));
    }

    @Test
    void putComIdDivergenteRetorna422() throws Exception {
        mvc.perform(put("/api/restaurante").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":999999,"nome":"Outro","cnpj":"04252011000110"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("não corresponde")));
    }

    @Test
    void putSemRestauranteRetorna404() throws Exception {
        jdbc.update("DELETE FROM restaurante");

        mvc.perform(put("/api/restaurante").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"nome":"Renomeado","cnpj":"04252011000110"}
                                """.formatted(restauranteId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void putComCnpjInvalidoRetorna400() throws Exception {
        mvc.perform(put("/api/restaurante").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"nome":"Renomeado","cnpj":"11222333000180"}
                                """.formatted(restauranteId)))
                .andExpect(status().isBadRequest());

        assertEquals("04252011000110", jdbc.queryForObject(
                "SELECT cnpj FROM restaurante WHERE id = ?", String.class, restauranteId));
    }

    @Test
    void putSemNomeRetorna400() throws Exception {
        mvc.perform(put("/api/restaurante").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"nome":"","cnpj":"04252011000110"}
                                """.formatted(restauranteId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putComCnpjDeOutroRestauranteRetorna409() throws Exception {
        criaSegundoRestaurante();

        mvc.perform(put("/api/restaurante").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"nome":"Renomeado","cnpj":"11222333000181"}
                                """.formatted(restauranteId)))
                .andExpect(status().isConflict());
    }

    @Test
    void postNaoExisteRetorna405() throws Exception {
        mvc.perform(post("/api/restaurante").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.mensagem").value("Método não suportado"));
    }

    /** Segundo restaurante so pra ter CNPJ em uso por outra linha. */
    private void criaSegundoRestaurante() {
        garanteCidade();
        if (contagem("endereco", SECUNDARIO) == 0) {
            jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (902, 'Avenida', '2', 901)");
        }
        if (contagem("configuracao", SECUNDARIO) == 0) {
            jdbc.update("INSERT INTO configuracao(id) VALUES (902)");
        }
        jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (902, 'Segundo', '11222333000181', 902, 902)
                """);
    }

    private void garanteCidade() {
        if (contagem("cidade", PRINCIPAL) > 0) {
            return;
        }
        if (contagem("estado", PRINCIPAL) == 0) {
            jdbc.update("INSERT INTO estado(id, nome) VALUES (901, 'Teste')");
        }
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (901, 'Teste', 901)");
    }

    /** Quantas linhas da tabela existem. Com id nulo, conta todas. */
    private int contagem(String tabela, Integer id) {
        String sql = id == null
                ? "SELECT COUNT(*) FROM " + tabela
                : "SELECT COUNT(*) FROM " + tabela + " WHERE id = ?";
        Integer total = id == null
                ? jdbc.queryForObject(sql, Integer.class)
                : jdbc.queryForObject(sql, Integer.class, id);
        return total == null ? 0 : total;
    }
}
