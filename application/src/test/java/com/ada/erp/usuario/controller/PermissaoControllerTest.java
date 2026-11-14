package com.ada.erp.usuario.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.restaurante.srv.application.Application;
import com.app.restaurante.srv.application.core.entity.Permissao;
import com.app.restaurante.srv.application.infra.repository.PermissaoCargoRepository;
import com.app.restaurante.srv.application.infra.repository.PermissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/** Cobre os criterios de aceite da PERM-02 (#64). */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class PermissaoControllerTest {

    private static final String CHAVE = "PEDIDOS_CRIAR";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PermissaoRepository repository;

    @MockitoBean
    private PermissaoCargoRepository permissaoCargoRepository;

    @BeforeEach
    void fixture() {
        repository.saveAndFlush(Permissao.builder()
                .chave(CHAVE)
                .nome("Criar pedido")
                .descricao("Permite criar pedido")
                .build());
    }

    @Test
    void criarRetorna201() throws Exception {
        mvc.perform(post("/api/permissoes").with(autoridade("PERMISSOES_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"PEDIDOS_EDITAR\",\"nome\":\"Editar pedido\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.chave").value("PEDIDOS_EDITAR"))
                .andExpect(jsonPath("$.nome").value("Editar pedido"));
    }

    @Test
    void criarComChaveDuplicadaRetorna409() throws Exception {
        mvc.perform(post("/api/permissoes").with(autoridade("PERMISSOES_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"" + CHAVE + "\",\"nome\":\"Outro rotulo\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void criarSemNomeRetorna400() throws Exception {
        mvc.perform(post("/api/permissoes").with(autoridade("PERMISSOES_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"PEDIDOS_EDITAR\",\"nome\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarAlterandoChaveRetorna422ComMensagem() throws Exception {
        mvc.perform(put("/api/permissoes/" + idDaChave()).with(autoridade("PERMISSOES_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"OUTRA_CHAVE\",\"nome\":\"Criar pedido\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("imutável")));
    }

    @Test
    void atualizarNomeRetorna200() throws Exception {
        mvc.perform(put("/api/permissoes/" + idDaChave()).with(autoridade("PERMISSOES_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Criar pedido novo\",\"descricao\":\"Atualizada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chave").value(CHAVE))
                .andExpect(jsonPath("$.nome").value("Criar pedido novo"));
    }

    @Test
    void listarComFiltroQ() throws Exception {
        mvc.perform(get("/api/permissoes").param("q", "pedidos").with(autoridade("PERMISSOES_VISUALIZAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chave").value(CHAVE));
        mvc.perform(get("/api/permissoes").param("q", "inexistente").with(autoridade("PERMISSOES_VISUALIZAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/permissoes/999999").with(autoridade("PERMISSOES_VISUALIZAR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirComVinculoRetorna409() throws Exception {
        Integer id = idDaChave();
        when(permissaoCargoRepository.existsByIdPermissao(id)).thenReturn(true);
        mvc.perform(delete("/api/permissoes/" + id).with(autoridade("PERMISSOES_EXCLUIR")))
                .andExpect(status().isConflict());
    }

    @Test
    void excluirSemVinculoRetorna204() throws Exception {
        mvc.perform(delete("/api/permissoes/" + idDaChave()).with(autoridade("PERMISSOES_EXCLUIR")))
                .andExpect(status().isNoContent());
    }

    @Test
    void semAuthorityRetorna403() throws Exception {
        mvc.perform(get("/api/permissoes").with(autoridade("OUTRA_PERMISSAO")))
                .andExpect(status().isForbidden());
    }

    private Integer idDaChave() {
        return repository.findByChave(CHAVE).orElseThrow().getId();
    }

    private RequestPostProcessor autoridade(String permissao) {
        return user("teste").authorities(new SimpleGrantedAuthority(permissao));
    }
}
