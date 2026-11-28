package com.app.restaurante.srv.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class FuncionarioCreateTests {

    private static final String ENDPOINT = "/api/funcionarios";
    private static final String CPF = "529.982.247-25";
    private static final String EMAIL = "cadastro@restaurante.com";
    private static final String SENHA = "SenhaInicial123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararEndereco() {
        jdbc.update("INSERT INTO estado(id, nome) VALUES (691, 'Parana FUNC-02')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (691, 'Medianeira', 691)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (691, 'Rua A', '10', 691)");
    }

    @Test
    void criaFuncionarioComHashERejeitaCpfEmOutroFormato() throws Exception {
        mvc.perform(comPermissao(payload(CPF, EMAIL, SENHA, 691)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value(EMAIL))
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.hash").doesNotExist());

        String hash = jdbc.queryForObject("SELECT senha FROM funcionario WHERE login = ?", String.class, EMAIL);
        assertThat(hash).startsWith("$2").isNotEqualTo(SENHA);
        assertThat(passwordEncoder.matches(SENHA, hash)).isTrue();

        mvc.perform(comPermissao(payload("52998224725", "outro@restaurante.com", SENHA, 691)))
                .andExpect(status().isConflict());
    }

    @Test
    void detectaCpfLegadoComMascara() throws Exception {
        jdbc.update("INSERT INTO perfil(nome, cpf, id_endereco) VALUES ('Perfil legado', ?, 691)", CPF);
        mvc.perform(comPermissao(payload("52998224725", EMAIL, SENHA, 691)))
                .andExpect(status().isConflict());
    }

    @Test
    void rejeitaCamposInvalidosEEnderecoInexistente() throws Exception {
        mvc.perform(comPermissao(payload("52998224724", EMAIL, SENHA, 691)))
                .andExpect(status().isBadRequest());
        mvc.perform(comPermissao(payload(CPF, "invalido", SENHA, 691)))
                .andExpect(status().isBadRequest());
        mvc.perform(comPermissao(payload(CPF, EMAIL, "curta", 691)))
                .andExpect(status().isBadRequest());
        mvc.perform(comPermissao(payload(CPF, EMAIL, "Senha123", 691)))
                .andExpect(status().isBadRequest());
        mvc.perform(comPermissao(payload(CPF, EMAIL, "é".repeat(37), 691)))
                .andExpect(status().isBadRequest());
        mvc.perform(comPermissao(payload(CPF, EMAIL, SENHA, 692)))
                .andExpect(status().isNotFound());
    }

    @Test
    void exigePermissaoDeCadastro() throws Exception {
        String body = payload(CPF, EMAIL, SENHA, 691);
        mvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(ENDPOINT).with(user("garcom"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    private MockHttpServletRequestBuilder comPermissao(String body) {
        return post(ENDPOINT)
                .with(user("gerente").authorities(new SimpleGrantedAuthority("FUNCIONARIOS_CRIAR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private String payload(String cpf, String email, String senha, int enderecoId) {
        return """
                {"nome":"Pessoa Teste","cpf":"%s","email":"%s","senha":"%s","enderecoId":%d}
                """.formatted(cpf, email, senha, enderecoId);
    }
}
