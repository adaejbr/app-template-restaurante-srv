package com.app.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Permissao;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.CargoRepository;

// Cobre o criterio de aceite da CARGO-01 (#65).
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CargoRepositoryTests {

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CargoFuncionarioRepository cargoFuncionarioRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void fixtures() {
        jdbc.update("INSERT INTO estado(id, nome) VALUES (901, 'Teste')");
        jdbc.update("INSERT INTO cidade(id, nome, id_estado) VALUES (901, 'Teste', 901)");
        jdbc.update("INSERT INTO endereco(id, logradouro, numero, id_cidade) VALUES (901, 'Rua', '1', 901)");
        jdbc.update("INSERT INTO configuracao(id) VALUES (901)");
        jdbc.update("""
                INSERT INTO restaurante(id, nome, cnpj, id_endereco, id_configuracao)
                VALUES (901, 'Restaurante', '00000000000000', 901, 901)
                """);
        jdbc.update("INSERT INTO setor(id, nome, id_restaurante) VALUES (901, 'Administrativo', 901)");
        jdbc.update("INSERT INTO cargo(id, nome, id_setor) VALUES (901, 'Gerente', 901)");
        jdbc.update("""
                INSERT INTO permissao(id, chave, nome) VALUES
                    (901, 'PEDIDOS_CRIAR', 'Criar pedido'),
                    (902, 'PEDIDOS_EXCLUIR', 'Excluir pedido')
                """);
        jdbc.update("""
                INSERT INTO permissao_cargo(id_permissao, id_cargo, situacao) VALUES
                    (901, 901, 'ATIVA'),
                    (902, 901, 'INATIVA')
                """);
        jdbc.update("INSERT INTO perfil(id, nome, cpf, id_endereco) VALUES (901, 'Gerente', '00000000000', 901)");
        jdbc.update("""
                INSERT INTO funcionario(id, id_perfil, login, senha, status)
                VALUES (901, 901, 'gerente@teste.com', 'x', 'ATIVO')
                """);
        jdbc.update("INSERT INTO cargo_funcionario(id_cargo, id_funcionario, status) VALUES (901, 901, 'ATIVO')");
    }

    @Test
    void getPermissoesCarregaSomenteAsAtivas() {
        Set<String> chaves = cargoRepository.findById(901).orElseThrow().getPermissoes().stream()
                .map(Permissao::getChave)
                .collect(Collectors.toSet());

        assertEquals(Set.of("PEDIDOS_CRIAR"), chaves);
    }

    @Test
    void findByCargoNomeAndStatusRetornaVinculoAtivo() {
        List<CargoFuncionario> vinculos =
                cargoFuncionarioRepository.findByCargoNomeAndStatus("Gerente", "ATIVO");

        assertEquals(1, vinculos.size());
        assertEquals(901, vinculos.get(0).getIdFuncionario());
    }
}
