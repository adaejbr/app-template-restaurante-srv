package com.ada.erp.cargo.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** Cobre o CRUD e o criterio de aceite da CARGO-02 (#66). */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class CargoFuncionarioControllerTest {

    private static final Integer SETOR = 961;
    private static final Integer CARGO_ATIVO = 961;
    private static final Integer CARGO_INATIVO = 962;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void fixtures() {
        jdbc.update("INSERT INTO estado(id, nome) VALUES (961, 'Teste CARGO-02')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (961, 'Teste', 961)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (961, 'Rua', '1', 961)");
        jdbc.update("INSERT INTO configuracao(id) VALUES (961)");
        jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (961, 'Restaurante', '00000000000066', 961, 961)
                """);
        jdbc.update("INSERT INTO setor(id, nome, id_restaurante) VALUES (961, 'Salão', 961)");
        jdbc.update("""
                INSERT INTO cargo(id, nome, id_setor, status) VALUES
                    (961, 'Gerente', 961, 'ATIVO'),
                    (962, 'Caixa Antigo', 961, 'INATIVO')
                """);
        jdbc.update("""
                INSERT INTO permissao(id, chave, nome) VALUES
                    (961, 'PEDIDOS_CRIAR', 'Criar pedido'),
                    (962, 'PEDIDOS_EDITAR', 'Editar pedido'),
                    (963, 'PEDIDOS_EXCLUIR', 'Excluir pedido')
                """);
        jdbc.update("""
                INSERT INTO permissao_cargo(id_permissao, id_cargo, situacao) VALUES
                    (961, 961, 'ATIVA'),
                    (962, 961, 'INATIVA')
                """);
    }

    @Test
    void buscarRetornaCargoComPermissoesAtivasEmbutidas() throws Exception {
        mvc.perform(get("/api/cargos/%d".formatted(CARGO_ATIVO))
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(CARGO_ATIVO))
                .andExpect(jsonPath("$.data.nome").value("Gerente"))
                .andExpect(jsonPath("$.data.idSetor").value(SETOR))
                .andExpect(jsonPath("$.data.status").value("ATIVO"))
                .andExpect(jsonPath("$.data.ativo").value(true))
                .andExpect(jsonPath("$.data.permissoes.length()").value(1))
                .andExpect(jsonPath("$.data.permissoes[0].chave").value("PEDIDOS_CRIAR"));
    }

    @Test
    void buscarCargoInativoOuInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/cargos/%d".formatted(CARGO_INATIVO))
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/cargos/999999")
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarRespeitaStatusAtivoERetornaPermissoesEmbutidas() throws Exception {
        mvc.perform(get("/api/cargos")
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == 961)].nome").value("Gerente"))
                .andExpect(jsonPath("$.data[?(@.id == 961)].permissoes[0].chave").value("PEDIDOS_CRIAR"))
                .andExpect(jsonPath("$.data[?(@.id == 962)]").isEmpty());
    }

    @Test
    void criarComPermissoesRetorna201() throws Exception {
        mvc.perform(post("/api/cargos")
                        .with(autoridade("CARGOS_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Garçom","idSetor":961,"permissoes":[961,963]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nome").value("Garçom"))
                .andExpect(jsonPath("$.data.idSetor").value(SETOR))
                .andExpect(jsonPath("$.data.status").value("ATIVO"))
                .andExpect(jsonPath("$.data.ativo").value(true))
                .andExpect(jsonPath("$.data.permissoes.length()").value(2))
                .andExpect(jsonPath("$.data.permissoes[0].chave").value("PEDIDOS_CRIAR"))
                .andExpect(jsonPath("$.data.permissoes[1].chave").value("PEDIDOS_EXCLUIR"));
    }

    @Test
    void criarComNomeDuplicadoNoSetorRetorna409() throws Exception {
        mvc.perform(post("/api/cargos")
                        .with(autoridade("CARGOS_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Gerente\",\"idSetor\":961}"))
                .andExpect(status().isConflict());
    }

    @Test
    void criarComSetorInexistenteRetorna404() throws Exception {
        mvc.perform(post("/api/cargos")
                        .with(autoridade("CARGOS_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Recepcionista\",\"idSetor\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarComPermissaoInexistenteRetorna422() throws Exception {
        mvc.perform(post("/api/cargos")
                        .with(autoridade("CARGOS_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Recepcionista\",\"idSetor\":961,\"permissoes\":[9999]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("9999")));
    }

    @Test
    void criarSemNomeRetorna400() throws Exception {
        mvc.perform(post("/api/cargos")
                        .with(autoridade("CARGOS_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"idSetor\":961}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarCargoEPermissoesRetorna200() throws Exception {
        mvc.perform(put("/api/cargos/%d".formatted(CARGO_ATIVO))
                        .with(autoridade("CARGOS_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Gerente Geral","idSetor":961,"permissoes":[962,963]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Gerente Geral"))
                .andExpect(jsonPath("$.data.permissoes.length()").value(2))
                .andExpect(jsonPath("$.data.permissoes[0].chave").value("PEDIDOS_EDITAR"))
                .andExpect(jsonPath("$.data.permissoes[1].chave").value("PEDIDOS_EXCLUIR"));
    }

    @Test
    void desativarMudaStatusEOcultaNoGet() throws Exception {
        mvc.perform(delete("/api/cargos/%d".formatted(CARGO_ATIVO))
                        .with(autoridade("CARGOS_EXCLUIR")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/cargos/%d".formatted(CARGO_ATIVO))
                        .with(autoridade("CARGOS_VISUALIZAR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void semAuthorityRetorna403() throws Exception {
        mvc.perform(get("/api/cargos")
                        .with(autoridade("PERMISSOES_VISUALIZAR")))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor autoridade(String permissao) {
        return user("teste").authorities(new SimpleGrantedAuthority(permissao));
    }
}
