package com.app.restaurante.srv.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import java.util.List;
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
 * Testes de integração para atualização de funcionário com senha opcional (FUNC-03, #70).
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Transactional
class FuncionarioUpdateTests {

    private static final Integer CARGO_GERENTE = 981;
    private static final Integer CARGO_GARCOM = 982;
    private static final Integer FUNC_PRINCIPAL = 981;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private CargoFuncionarioRepository cargoFuncionarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String hashOriginal;

    @BeforeEach
    void fixtures() {
        hashOriginal = passwordEncoder.encode("senhaOriginal123");

        jdbc.update("INSERT INTO estado(id, nome) VALUES (981, 'Parana FUNC-03')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (981, 'Medianeira', 981)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (981, 'Rua A', '10', 981)");
        jdbc.update("INSERT INTO configuracao(id) VALUES (981)");
        jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (981, 'Restaurante FUNC-03', '00000000000098', 981, 981)
                """);
        jdbc.update("INSERT INTO setor(id, nome, id_restaurante) VALUES (981, 'Salao', 981)");
        jdbc.update("""
                INSERT INTO cargo(id, nome, id_setor) VALUES
                    (981, 'Gerente', 981),
                    (982, 'Garcom', 981)
                """);
        jdbc.update("""
                INSERT INTO perfil(id, nome, cpf, telefone, id_endereco) VALUES
                    (981, 'Maria Original', '111.111.111-11', '(45) 99999-0001', 981),
                    (982, 'Joao Outro', '222.222.222-22', '(45) 99999-0002', 981)
                """);
        jdbc.update("""
                INSERT INTO funcionario(id, id_perfil, login, senha, status) VALUES
                    (981, 981, 'maria@restaurante.com', ?, 'ATIVO'),
                    (982, 982, 'joao@restaurante.com', ?, 'ATIVO')
                """, hashOriginal, hashOriginal);
        jdbc.update("""
                INSERT INTO cargo_funcionario(id_cargo, id_funcionario, status) VALUES
                    (981, 981, 'ATIVO'),
                    (982, 982, 'ATIVO')
                """);
    }

    @Test
    void putSemSenhaMantemHashOriginal() throws Exception {
        String payload = """
                {
                    "nome": "Maria Atualizada",
                    "cpf": "111.111.111-11",
                    "telefone": "(45) 98888-1111",
                    "login": "maria.nova@restaurante.com",
                    "idCargo": 981,
                    "status": "ATIVO"
                }
                """;

        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FUNC_PRINCIPAL))
                .andExpect(jsonPath("$.nome").value("Maria Atualizada"))
                .andExpect(jsonPath("$.login").value("maria.nova@restaurante.com"))
                .andExpect(jsonPath("$.telefone").value("(45) 98888-1111"))
                .andExpect(jsonPath("$.idCargo").value(CARGO_GERENTE))
                .andExpect(jsonPath("$.nomeCargo").value("Gerente"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        Funcionario atualizado = funcionarioRepository.findById(FUNC_PRINCIPAL).orElseThrow();
        assertThat(atualizado.getSenha()).isEqualTo(hashOriginal);
    }

    @Test
    void putComSenhaEmBrancoMantemHashOriginal() throws Exception {
        String payload = """
                {
                    "nome": "Maria Sem Troca Senha",
                    "cpf": "111.111.111-11",
                    "telefone": "(45) 99999-0001",
                    "login": "maria@restaurante.com",
                    "senha": "   ",
                    "idCargo": 981
                }
                """;

        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Sem Troca Senha"));

        Funcionario atualizado = funcionarioRepository.findById(FUNC_PRINCIPAL).orElseThrow();
        assertThat(atualizado.getSenha()).isEqualTo(hashOriginal);
    }

    @Test
    void putComNovaSenhaGeraHashDiferente() throws Exception {
        String payload = """
                {
                    "nome": "Maria Nova Senha",
                    "cpf": "111.111.111-11",
                    "telefone": "(45) 99999-0001",
                    "login": "maria@restaurante.com",
                    "senha": "novaSenhaSegura456",
                    "idCargo": 981,
                    "status": "ATIVO"
                }
                """;

        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Nova Senha"));

        Funcionario atualizado = funcionarioRepository.findById(FUNC_PRINCIPAL).orElseThrow();
        assertThat(atualizado.getSenha()).isNotEqualTo(hashOriginal);
        assertThat(passwordEncoder.matches("novaSenhaSegura456", atualizado.getSenha())).isTrue();
    }

    @Test
    void putTrocaCargoInativaAnteriorEAtivaNovo() throws Exception {
        String payload = """
                {
                    "nome": "Maria Garcom",
                    "cpf": "111.111.111-11",
                    "telefone": "(45) 99999-0001",
                    "login": "maria@restaurante.com",
                    "idCargo": 982
                }
                """;

        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCargo").value(CARGO_GARCOM))
                .andExpect(jsonPath("$.nomeCargo").value("Garcom"));

        List<CargoFuncionario> vinculos = cargoFuncionarioRepository.findByIdFuncionario(FUNC_PRINCIPAL);
        assertThat(vinculos)
                .anyMatch(v -> v.getIdCargo().equals(CARGO_GERENTE) && "INATIVO".equals(v.getStatus()))
                .anyMatch(v -> v.getIdCargo().equals(CARGO_GARCOM) && "ATIVO".equals(v.getStatus()));
    }

    @Test
    void putComCpfDeOutroFuncionarioRetorna409() throws Exception {
        String payload = """
                {
                    "nome": "Maria",
                    "cpf": "222.222.222-22",
                    "login": "maria@restaurante.com",
                    "idCargo": 981
                }
                """;

        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void putComLoginDeOutroFuncionarioRetorna409() throws Exception {
        String payload = """
                {
                    "nome": "Maria",
                    "cpf": "111.111.111-11",
                    "login": "joao@restaurante.com",
                    "idCargo": 981
                }
                """;

        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void putComCargoInexistenteRetorna404() throws Exception {
        String payload = """
                {
                    "nome": "Maria",
                    "cpf": "111.111.111-11",
                    "login": "maria@restaurante.com",
                    "idCargo": 999999
                }
                """;

        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void putEmFuncionarioInexistenteRetorna404() throws Exception {
        String payload = """
                {
                    "nome": "Inexistente",
                    "cpf": "333.333.333-33",
                    "login": "inexistente@restaurante.com",
                    "idCargo": 981
                }
                """;

        mvc.perform(put("/api/funcionarios/999999")
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void putSemAutenticacaoRetorna401() throws Exception {
        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putComPayloadInvalidoRetorna400() throws Exception {
        mvc.perform(put("/api/funcionarios/%d".formatted(FUNC_PRINCIPAL))
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }
}
