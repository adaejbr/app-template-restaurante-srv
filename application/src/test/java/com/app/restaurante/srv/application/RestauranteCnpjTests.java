package com.app.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.restaurante.srv.application.core.entity.Restaurante;
import com.app.restaurante.srv.application.infra.repository.RestauranteRepository;
import jakarta.validation.ConstraintViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.app.restaurante.srv.application.infra.repository.EnderecoRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RestauranteCnpjTests {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected RestauranteRepository repository;

    @Autowired
    protected EnderecoRepository enderecoRepository;

    protected int restauranteId;

    @BeforeEach
    void fixtures() {
        var ids = jdbc.query(
                "SELECT id FROM restaurante ORDER BY id LIMIT 1",
                (rs, rowNum) -> rs.getInt("id")
        );

        if (ids.isEmpty()) {
            // Cria dados de teste somente quando o banco não possui restaurante.
            jdbc.update("INSERT INTO estado(id, nome) VALUES (901, 'Teste')");
            jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (901, 'Teste', 901)");

            jdbc.update("""
                INSERT INTO endereco(id, logradouro, numero, id_cidade)
                VALUES (901, 'Rua', '1', 901),
                    (902, 'Avenida', '2', 901)
                """);

            jdbc.update("INSERT INTO configuracao(id) VALUES (901)");

            jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (901, 'Primeiro', '04252011000110', 901, 901)
                """);

            restauranteId = 901;
        } else {
            restauranteId = ids.get(0);
        }

        // Padroniza o restaurante utilizado pelos testes.
        jdbc.update("""
            UPDATE restaurante
            SET nome = ?, cnpj = ?
            WHERE id = ?
            """, "Primeiro", "04252011000110", restauranteId);
    }


    @Test
    void sameRestaurantCanKeepItsCnpjAndPersistsCanonicalForm() throws Exception {
        mvc.perform(patch("/api/restaurantes/{id}/cnpj", restauranteId)
                        .with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cnpj\":\"04.252.011/0001-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value("04252011000110"))
                .andExpect(jsonPath("$.id").value(restauranteId));

        assertEquals("04252011000110", storedCnpj(restauranteId));
        assertEquals("Primeiro",
                jdbc.queryForObject(
                        "SELECT nome FROM restaurante WHERE id = ?",
                        String.class,
                        restauranteId
                ));
    }

    @Test
    void alphanumericInputIsUppercaseAndUnmaskedInDatabase() throws Exception {
        mvc.perform(patch("/api/restaurantes/{id}/cnpj", restauranteId).with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cnpj\":\"12.abc.345/01de-35\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value("12ABC34501DE35"));
        assertEquals("12ABC34501DE35", storedCnpj(restauranteId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"cnpj\":null}", "{\"cnpj\":\"\"}",
            "{\"cnpj\":\"11222333000180\"}", "{\"cnpj\":\"00000000000000\"}"})
    void invalidInputReturns400WithoutChangingDatabase(String payload) throws Exception {
        mvc.perform(patch("/api/restaurantes/{id}/cnpj", restauranteId).with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());
        assertEquals("04252011000110", storedCnpj(restauranteId));
    }

    @Test
    void missingRestaurantReturns404() throws Exception {
        mvc.perform(patch("/api/restaurantes/9999/cnpj").with(user("teste"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cnpj\":\"11222333000181\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void endpointRequiresAuthentication() throws Exception {
        mvc.perform(patch("/api/restaurantes/{id}/cnpj", restauranteId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cnpj\":\"11222333000181\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void directRepositoryUpdateAlsoNormalizesCnpj() {
        Restaurante restaurante = repository.findById(restauranteId).orElseThrow();
        restaurante.setCnpj("12.abc.345/01de-35");
        repository.saveAndFlush(restaurante);
        assertEquals("12ABC34501DE35", storedCnpj(restauranteId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000180", "11--222333000181", "12ßC34501DE33"})
    void directRepositoryRejectsInvalidCnpj(String cnpj) {
        Restaurante restaurante = repository.findById(restauranteId).orElseThrow();
        restaurante.setCnpj(cnpj);
        assertThrows(ConstraintViolationException.class, () -> repository.saveAndFlush(restaurante));
    }


    protected String storedCnpj(int id) {
        return jdbc.queryForObject("SELECT cnpj FROM restaurante WHERE id = ?", String.class, id);
    }
}
