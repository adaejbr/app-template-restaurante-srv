package com.app.restaurante.srv.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.restaurante.srv.application.Application;
import jakarta.persistence.EntityManager;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

// Cobre o criterio de aceite da FUNC-05 (#67): o gerente altera a senha de outro
// funcionario sem a senha atual, o banco guarda um hash novo e a auditoria
// PASSWORD_CHANGE e registrada com o autor e a conta afetada.
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class FuncionarioSenhaControllerTest {

    private static final int GERENTE = 981;
    private static final int ALVO = 982;
    private static final String LOGIN_GERENTE = "gerente.func05@teste.com";
    private static final String SENHA_ANTIGA = "Senha-Antiga-Func05";
    private static final String SENHA_NOVA = "Cozinha-Temporaria-2026";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String hashAntigo;

    @BeforeEach
    void fixtures() {
        hashAntigo = passwordEncoder.encode(SENHA_ANTIGA);
        jdbc.update("INSERT INTO estado(id, nome) VALUES (981, 'Teste FUNC-05')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (981, 'Teste', 981)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (981, 'Rua', '1', 981)");
        jdbc.update("""
                INSERT INTO perfil(id, nome, cpf, id_endereco) VALUES
                    (981, 'Gerente FUNC-05', '98100000001', 981),
                    (982, 'Garcom FUNC-05', '98200000002', 981)
                """);
        jdbc.update("""
                INSERT INTO funcionario(id, id_perfil, login, senha, status) VALUES
                    (?, 981, ?, ?, 'ATIVO'),
                    (?, 982, 'garcom.func05@teste.com', ?, 'ATIVO')
                """, GERENTE, LOGIN_GERENTE, hashAntigo, ALVO, hashAntigo);
    }

    @Test
    void gerenteAlteraSenhaDeOutroFuncionarioSemSenhaAtual(CapturedOutput output) throws Exception {
        alterarSenha(ALVO, SENHA_NOVA, gerente())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Map<String, Object> linha = linhaDoFuncionario(ALVO);
        String hashNovo = (String) linha.get("senha");
        assertThat(hashNovo).isNotEqualTo(hashAntigo).isNotEqualTo(SENHA_NOVA);
        assertThat(passwordEncoder.matches(SENHA_NOVA, hashNovo)).isTrue();
        assertThat(passwordEncoder.matches(SENHA_ANTIGA, hashNovo)).isFalse();
        assertThat(linha.get("deve_alterar_senha")).isEqualTo(true);

        assertThat(output).contains("auditoria acao=PASSWORD_CHANGE entidadeId=" + ALVO
                + " usuario=" + LOGIN_GERENTE);
        assertThat(output).doesNotContain(SENHA_NOVA);
    }

    @Test
    void gerenteNaoAlteraAPropriaSenhaPorEsteEndpoint() throws Exception {
        alterarSenha(GERENTE, SENHA_NOVA, gerente())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(containsString("senha atual")));

        Map<String, Object> linha = linhaDoFuncionario(GERENTE);
        assertThat(linha.get("senha")).isEqualTo(hashAntigo);
        assertThat(linha.get("deve_alterar_senha")).isEqualTo(false);
    }

    @Test
    void autoAlteracaoIgnoraMaiusculasDoLogin() throws Exception {
        alterarSenha(GERENTE, SENHA_NOVA,
                user(LOGIN_GERENTE.toUpperCase()).authorities(new SimpleGrantedAuthority("FUNCIONARIOS_EDITAR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void funcionarioInexistenteRetorna404() throws Exception {
        alterarSenha(999_999, SENHA_NOVA, gerente())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Funcionário não encontrado"));
    }

    @Test
    void senhaCurtaRetorna400() throws Exception {
        alterarSenha(ALVO, "Ab1-xyz", gerente())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("mínimo 8")));
        assertThat(linhaDoFuncionario(ALVO).get("senha")).isEqualTo(hashAntigo);
    }

    @Test
    void senhaComumRetorna400() throws Exception {
        alterarSenha(ALVO, "Senha123", gerente())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("comum")));
    }

    @Test
    void senhaAcimaDoLimiteDoBcryptRetorna400() throws Exception {
        // 40 caracteres, mas 80 bytes em UTF-8: passa no limite de caracteres e nao no de bytes.
        alterarSenha(ALVO, "ç".repeat(40), gerente())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("72 bytes")));
    }

    @Test
    void senhaAusenteRetorna400() throws Exception {
        mvc.perform(patch("/api/funcionarios/%d/senha".formatted(ALVO))
                        .with(gerente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semPermissaoRetorna403() throws Exception {
        alterarSenha(ALVO, SENHA_NOVA,
                user(LOGIN_GERENTE).authorities(new SimpleGrantedAuthority("FUNCIONARIOS_VISUALIZAR")))
                .andExpect(status().isForbidden());
        assertThat(linhaDoFuncionario(ALVO).get("senha")).isEqualTo(hashAntigo);
    }

    @Test
    void semAutenticacaoRetorna401() throws Exception {
        mvc.perform(patch("/api/funcionarios/%d/senha".formatted(ALVO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaSenha\":\"%s\"}".formatted(SENHA_NOVA)))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions alterarSenha(int id, String novaSenha, RequestPostProcessor autor) throws Exception {
        return mvc.perform(patch("/api/funcionarios/%d/senha".formatted(id))
                .with(autor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"novaSenha\":\"%s\"}".formatted(novaSenha)));
    }

    private RequestPostProcessor gerente() {
        return user(LOGIN_GERENTE).authorities(new SimpleGrantedAuthority("FUNCIONARIOS_EDITAR"));
    }

    /** Le direto da tabela, depois do flush, para provar o que foi persistido. */
    private Map<String, Object> linhaDoFuncionario(int id) {
        entityManager.flush();
        return jdbc.queryForMap("SELECT senha, deve_alterar_senha FROM funcionario WHERE id = ?", id);
    }
}
