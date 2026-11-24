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
class FuncionarioListingTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("INSERT INTO estado (id, nome) VALUES (961, 'Parana')");
        jdbc.update("INSERT INTO cidade (id, nome, id_estado) VALUES (961, 'Medianeira', 961)");
        jdbc.update("INSERT INTO endereco (id, logradouro, numero, id_cidade) VALUES (961, 'Rua A', '10', 961)");

        Long restauranteId = jdbc.query(
                "SELECT id FROM restaurante LIMIT 1",
                rs -> rs.next() ? rs.getLong("id") : null);

        if (restauranteId == null) {
            jdbc.update("INSERT INTO configuracao (id) VALUES (961)");
            jdbc.update("""
                    INSERT INTO restaurante (id, nome, cnpj, id_endereco, id_configuracao)
                    VALUES (961, 'Restaurante Teste', '04252011000110', 961, 961)
                    """);
            restauranteId = 961L;
        }

        jdbc.update("INSERT INTO setor (id, nome, id_restaurante) VALUES (961, 'Salao', ?)", restauranteId);
        jdbc.update("INSERT INTO cargo (id, nome, id_setor) VALUES (961, 'GARCOM', 961), (962, 'CAIXA', 961)");

        jdbc.update("""
                INSERT INTO perfil (id, nome, cpf, id_endereco)
                VALUES (961, 'Joao Carlos', '11122233344', 961),
                       (962, 'Joao Almeida', '22233344455', 961),
                       (963, 'Maria Souza', '33344455566', 961),
                       (964, 'Joao Inativo', '44455566677', 961)
                """);

        jdbc.update("""
                INSERT INTO funcionario (id, id_perfil, login, senha, status)
                VALUES (961, 961, 'joao.carlos@teste.com', 'segredo-hash-1', 'ATIVO'),
                       (962, 962, 'joao.almeida@teste.com', 'segredo-hash-2', 'ATIVO'),
                       (963, 963, 'maria@teste.com', 'segredo-hash-3', 'ATIVO'),
                       (964, 964, 'joao.inativo@teste.com', 'segredo-hash-4', 'INATIVO')
                """);

        jdbc.update("""
                INSERT INTO cargo_funcionario (id_cargo, id_funcionario, status)
                VALUES (961, 961, 'ATIVO'),
                       (962, 961, 'INATIVO'),
                       (961, 962, 'ATIVO'),
                       (962, 963, 'ATIVO'),
                       (961, 964, 'ATIVO')
                """);
    }

    @Test
    void listsFuncionariosWithFiltersPaginationDefaultSortAndOmitsPassword() throws Exception {
        mvc.perform(get("/api/funcionarios")
                        .with(user("teste"))
                        .param("q", "joao")
                        .param("cargoId", "961")
                        .param("status", "ATIVO")
                        .param("page", "0")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(15))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(962))
                .andExpect(jsonPath("$.content[0].nome").value("Joao Almeida"))
                .andExpect(jsonPath("$.content[0].cpf").value("22233344455"))
                .andExpect(jsonPath("$.content[0].email").value("joao.almeida@teste.com"))
                .andExpect(jsonPath("$.content[0].cargoId").value(961))
                .andExpect(jsonPath("$.content[0].cargoNome").value("GARCOM"))
                .andExpect(jsonPath("$.content[0].status").value("ATIVO"))
                .andExpect(jsonPath("$.content[0].senha").doesNotExist())
                .andExpect(jsonPath("$.content[1].id").value(961))
                .andExpect(jsonPath("$.content[1].nome").value("Joao Carlos"));
    }

    @Test
    void supportsCustomSortByNomeDescAndFilterByCargoIdOnly() throws Exception {
        mvc.perform(get("/api/funcionarios")
                        .with(user("teste"))
                        .param("cargoId", "961")
                        .param("status", "ATIVO")
                        .param("sort", "nome,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("Joao Carlos"))
                .andExpect(jsonPath("$.content[1].nome").value("Joao Almeida"));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/funcionarios"))
                .andExpect(status().isUnauthorized());
    }
}
