package com.ada.erp.shared.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

/**
 * Cobre o criterio de aceite da INFRA-03 (#56): o advice central devolve 409
 * para duplicidade e 422 para violacao de regra de negocio, com a mensagem
 * resolvida no bundle em pt-BR.
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class GlobalExceptionHandlerTest {

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
    void duplicidadeRetorna409ComMensagemDoBundle() throws Exception {
        mvc.perform(post("/api/permissoes").with(autoridade("PERMISSOES_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"" + CHAVE + "\",\"nome\":\"Outro rotulo\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Chave de permissão já cadastrada"));
    }

    @Test
    void regraDeNegocioRetorna422ComMensagemDoBundle() throws Exception {
        mvc.perform(put("/api/permissoes/" + idDaChave()).with(autoridade("PERMISSOES_EDITAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"OUTRA_CHAVE\",\"nome\":\"Criar pedido\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("imutável")));
    }

    @Test
    void recursoInexistenteRetorna404ComMensagemDoBundle() throws Exception {
        mvc.perform(get("/api/permissoes/999999").with(autoridade("PERMISSOES_VISUALIZAR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Permissão não encontrada"));
    }

    @Test
    void corpoInvalidoRetorna400ComCampoEValidacaoEmPtBr() throws Exception {
        mvc.perform(post("/api/permissoes").with(autoridade("PERMISSOES_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"PEDIDOS_EDITAR\",\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("nome: Campo obrigatório")));
    }

    @Test
    void payloadIlegivelRetorna400ComMensagem() throws Exception {
        mvc.perform(post("/api/permissoes").with(autoridade("PERMISSOES_CRIAR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{isso nao e json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Payload inválido"));
    }

    @Test
    void acessoNegadoRetorna403ComMensagem() throws Exception {
        mvc.perform(get("/api/permissoes").with(autoridade("OUTRA_PERMISSAO")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Acesso negado"));
    }

    @Test
    void rotaInexistenteRetorna404EmVezDe500() throws Exception {
        mvc.perform(get("/api/rota-que-nao-existe").with(autoridade("PERMISSOES_VISUALIZAR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Recurso não encontrado"));
    }

    private Integer idDaChave() {
        return repository.findByChave(CHAVE).orElseThrow().getId();
    }

    private RequestPostProcessor autoridade(String permissao) {
        return user("teste").authorities(new SimpleGrantedAuthority(permissao));
    }
}
