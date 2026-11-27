package com.app.restaurante.srv.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração para exclusão lógica de funcionário e bloqueio de login (FUNC-04, #71).
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class FuncionarioDesativarTests {

    private static final Integer FUNC_ALVO = 991;
    private static final String LOGIN_ALVO = "carlos.desativar@restaurante.com";
    private static final String SENHA_ALVO = "senhaSegura123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void fixtures() {
        String hash = passwordEncoder.encode(SENHA_ALVO);

        jdbc.update("INSERT INTO estado(id, nome) VALUES (991, 'Parana FUNC-04')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (991, 'Medianeira', 991)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (991, 'Rua B', '20', 991)");
        jdbc.update("INSERT INTO configuracao(id) VALUES (991)");
        jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (991, 'Restaurante FUNC-04', '00000000000099', 991, 991)
                """);
        jdbc.update("INSERT INTO setor(id, nome, id_restaurante) VALUES (991, 'Salao', 991)");
        jdbc.update("INSERT INTO cargo(id, nome, id_setor) VALUES (991, 'Garcom', 991)");
        jdbc.update("""
                INSERT INTO perfil(id, nome, cpf, telefone, id_endereco) VALUES
                    (991, 'Carlos Desativar', '555.555.555-55', '(45) 99999-0099', 991)
                """);
        jdbc.update("""
                INSERT INTO funcionario(id, id_perfil, login, senha, status) VALUES
                    (991, 991, ?, ?, 'ATIVO')
                """, LOGIN_ALVO, hash);
        jdbc.update("""
                INSERT INTO cargo_funcionario(id_cargo, id_funcionario, status) VALUES
                    (991, 991, 'ATIVO')
                """);
    }

    @Test
    void deleteDesativaFuncionarioEMantemNoBanco() throws Exception {
        mvc.perform(delete("/api/funcionarios/%d".formatted(FUNC_ALVO))
                        .with(user("admin")))
                .andExpect(status().isNoContent());

        Funcionario funcionario = funcionarioRepository.findById(FUNC_ALVO).orElseThrow();
        assertThat(funcionario.getStatus()).isEqualTo("INATIVO");
    }

    @Test
    void funcionarioDesativadoNaoApareceNaListagemDefault() throws Exception {
        mvc.perform(delete("/api/funcionarios/%d".formatted(FUNC_ALVO))
                        .with(user("admin")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/funcionarios")
                        .with(user("admin"))
                        .param("q", LOGIN_ALVO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(get("/api/funcionarios")
                        .with(user("admin"))
                        .param("q", LOGIN_ALVO)
                        .param("status", "INATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(FUNC_ALVO))
                .andExpect(jsonPath("$.content[0].status").value("INATIVO"));
    }

    @Test
    void loginDeFuncionarioDesativadoRetorna401() throws Exception {
        String loginPayload = """
                {
                    "email": "%s",
                    "senha": "%s"
                }
                """.formatted(LOGIN_ALVO, SENHA_ALVO);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/funcionarios/%d".formatted(FUNC_ALVO))
                        .with(user("admin")))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteDeFuncionarioInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/api/funcionarios/999999")
                        .with(user("admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void deleteSemAutenticacaoRetorna401() throws Exception {
        mvc.perform(delete("/api/funcionarios/%d".formatted(FUNC_ALVO)))
                .andExpect(status().isUnauthorized());
    }
}
