package com.app.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Endereco;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.core.entity.Perfil;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.PerfilRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FuncionarioModeloTests {
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FuncionarioRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PerfilRepository perfis;

    @Autowired
    private CargoFuncionarioRepository cargos;

    @BeforeEach
    void fixtures() {
        jdbc.update("INSERT INTO estado(id, nome) VALUES (951, 'Teste FUNC-01')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (951, 'Teste', 951)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (951, 'Rua', '1', 951)");
        jdbc.update("INSERT INTO configuracao(id) VALUES (951)");
        jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (951, 'Teste', '04252011000110', 951, 951)
                """);
        jdbc.update("INSERT INTO setor(id, nome, id_restaurante) VALUES (951, 'Sala', 951)");
        jdbc.update("INSERT INTO cargo(id, nome, id_setor) VALUES (951, 'GARCOM', 951), (952, 'CAIXA', 951)");
        jdbc.update("""
                INSERT INTO perfil(id, nome, cpf, id_endereco)
                VALUES (951, 'Ana', '12345678901', 951), (952, 'Bia', '98765432100', 951)
                """);
        jdbc.update("""
                INSERT INTO funcionario(id, id_perfil, login, senha, status)
                VALUES (951, 951, 'ana@teste.com', 'hash-de-teste', 'ATIVO')
                """);
        jdbc.update("""
                INSERT INTO cargo_funcionario(id_cargo, id_funcionario, status)
                VALUES (951, 951, 'ATIVO'), (952, 951, 'INATIVO')
                """);
        jdbc.update("INSERT INTO permissao(id, nome) VALUES (951, 'ROLE_MEU_PERFIL_VIEW')");
        jdbc.update("INSERT INTO permissao_cargo(id_permissao, id_cargo, situacao) VALUES (951, 951, 'ATIVA')");
    }

    @Test
    void persistsProfileEmployeeAndRoleUsingExistingRepositories() {
        var perfil = perfis.saveAndFlush(Perfil.builder().nome("Carlos").cpf("11122233344")
                .endereco(entityManager.getReference(Endereco.class, 951)).build());
        var funcionario = repository.saveAndFlush(Funcionario.builder().perfil(perfil)
                .login("carlos@teste.com").senha("hash-de-teste").status("ATIVO").build());
        cargos.saveAndFlush(CargoFuncionario.builder().idCargo(951)
                .idFuncionario(funcionario.getId()).status("ATIVO").build());
        entityManager.clear();
        var persisted = repository.findDetalhadoById(funcionario.getId()).orElseThrow();
        assertEquals("11122233344", persisted.getPerfil().getCpf());
        assertEquals("GARCOM", persisted.getCargos().iterator().next().getCargo().getNome());
    }

    @Test
    void detailedQueryLoadsProfileRolesAndPermissionsWithoutOpenSession() {
        var funcionario = repository.findDetalhadoById(951).orElseThrow();
        entityManager.clear();
        assertEquals("Ana", funcionario.getPerfil().getNome());
        assertEquals(2, funcionario.getCargos().size());
        var vinculo = funcionario.getCargos().stream()
                .filter(cargo -> "ATIVO".equals(cargo.getStatus())).findFirst().orElseThrow();
        assertEquals("GARCOM", vinculo.getCargo().getNome());
        assertEquals("ROLE_MEU_PERFIL_VIEW", vinculo.getCargo().getPermissoes().iterator().next()
                .getPermissao().getNome());
        assertTrue(repository.findDetalhadoById(99999).isEmpty());
    }

    @Test
    void loginQueryKeepsProfileAvailableAndRolesLazy() {
        var funcionario = repository.findByLogin("ana@teste.com");
        assertTrue(Hibernate.isInitialized(funcionario.getPerfil()));
        assertFalse(Hibernate.isInitialized(funcionario.getCargos()));
        assertEquals(951, repository.findByEmail("ana@teste.com").getId());
    }

    @Test
    void employeeWithoutRoleCanStillBeLoaded() {
        jdbc.update("DELETE FROM cargo_funcionario WHERE id_funcionario = 951");
        var funcionario = repository.findDetalhadoById(951).orElseThrow();
        assertEquals("Ana", funcionario.getPerfil().getNome());
        assertTrue(funcionario.getCargos().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "UPDATE perfil SET cpf = '12345678901' WHERE id = 952",
        "UPDATE perfil SET id_endereco = 99999 WHERE id = 952",
        "UPDATE perfil SET id_endereco = NULL WHERE id = 952",
        "INSERT INTO funcionario(id_perfil, login, senha, status) VALUES (952, 'ana@teste.com', 'hash', 'ATIVO')",
        "INSERT INTO funcionario(id_perfil, login, senha, status) VALUES (951, 'outra@teste.com', 'hash', 'ATIVO')",
        "INSERT INTO funcionario(id_perfil, login, senha, status) VALUES (99999, 'nova@teste.com', 'hash', 'ATIVO')",
        "INSERT INTO funcionario(id_perfil, login, senha, status) VALUES (NULL, 'nova@teste.com', 'hash', 'ATIVO')",
        "INSERT INTO cargo_funcionario VALUES (99999, 951, 'ATIVO')",
        "INSERT INTO cargo_funcionario VALUES (951, 99999, 'ATIVO')",
        "INSERT INTO cargo_funcionario VALUES (951, 951, 'ATIVO')",
        "DELETE FROM perfil WHERE id = 951"
    })
    void databaseRejectsDuplicateAndInvalidRelationships(String sql) {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(sql));
    }
}
