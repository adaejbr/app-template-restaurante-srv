package com.ada.erp.cargo.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.restaurante.srv.application.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

// Cobre o criterio de aceite da CARGO-03 (#57): o PUT substitui o conjunto e o
// GET devolve o estado final.
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class CargoPermissaoControllerTest {

    private static final Integer CARGO = 971;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void fixtures() {
        jdbc.update("INSERT INTO estado(id, nome) VALUES (971, 'Teste CARGO-03')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (971, 'Teste', 971)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (971, 'Rua', '1', 971)");
        jdbc.update("INSERT INTO configuracao(id) VALUES (971)");
        jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (971, 'Restaurante', '00000000000001', 971, 971)
                """);
        jdbc.update("INSERT INTO setor(id, nome, id_restaurante) VALUES (971, 'Administrativo', 971)");
        jdbc.update("INSERT INTO cargo(id, nome, id_setor) VALUES (971, 'Gerente', 971)");
        jdbc.update("""
                INSERT INTO permissao(id, chave, nome) VALUES
                    (971, 'PEDIDOS_CRIAR', 'Criar pedido'),
                    (972, 'PEDIDOS_EDITAR', 'Editar pedido'),
                    (973, 'PEDIDOS_EXCLUIR', 'Excluir pedido'),
                    (974, 'PEDIDOS_CANCELAR', 'Cancelar pedido')
                """);
        // 971 comeca ATIVA e 974 comeca INATIVA para exercitar a reativacao.
        jdbc.update("""
                INSERT INTO permissao_cargo(id_permissao, id_cargo, situacao) VALUES
                    (971, 971, 'ATIVA'),
                    (974, 971, 'INATIVA')
                """);
    }

    @Test
    void putSubstituiOConjuntoEGDevolveEstadoFinal() throws Exception {
        mvc.perform(put("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissoes\":[972,973]}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].chave").value("PEDIDOS_EDITAR"))
                .andExpect(jsonPath("$.data[1].chave").value("PEDIDOS_EXCLUIR"));
    }

    @Test
    void putReativaVinculoQueJaExistiaComoInativo() throws Exception {
        mvc.perform(put("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissoes\":[974]}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].chave").value("PEDIDOS_CANCELAR"));
    }

    @Test
    void putComListaVaziaLimpaOConjunto() throws Exception {
        mvc.perform(put("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissoes\":[]}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void putEmCargoInexistenteRetorna404() throws Exception {
        mvc.perform(put("/api/cargos/999999/permissoes")
                        .with(autoridade("CARGOS_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissoes\":[971]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDeCargoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/cargos/999999/permissoes")
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void putComPermissaoInexistenteRetorna422ComMensagem() throws Exception {
        mvc.perform(put("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissoes\":[971,9999]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("9999")));
    }

    @Test
    void putSemListaRetorna400() throws Exception {
        mvc.perform(put("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("CARGOS_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semAuthorityRetorna403() throws Exception {
        mvc.perform(put("/api/cargos/%d/permissoes".formatted(CARGO))
                        .with(autoridade("PERMISSOES_VISUALIZAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissoes\":[971]}"))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor autoridade(String permissao) {
        return user("teste").authorities(new SimpleGrantedAuthority(permissao));
    }
}
