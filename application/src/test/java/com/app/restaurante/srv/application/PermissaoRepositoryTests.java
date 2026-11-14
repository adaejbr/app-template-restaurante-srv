package com.app.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.app.restaurante.srv.application.core.entity.Permissao;
import com.app.restaurante.srv.application.infra.repository.PermissaoRepository;

// Cobre o criterio de aceite da PERM-01 (#63).
// O perfil de teste usa H2 com ddl-auto=create-drop e Flyway desligado, entao o
// schema vem da entidade. O unique testado aqui e o da entidade, nao o da V8.
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PermissaoRepositoryTests {

    @Autowired
    private PermissaoRepository repository;

    @Test
    void findByChaveRetornaPermissao() {
        repository.saveAndFlush(permissao("PEDIDOS_CRIAR", "Criar pedido"));

        Optional<Permissao> encontrada = repository.findByChave("PEDIDOS_CRIAR");

        assertTrue(encontrada.isPresent());
        // Prova que chave e nome ficaram separados depois da V8.
        assertEquals("PEDIDOS_CRIAR", encontrada.get().getChave());
        assertEquals("Criar pedido", encontrada.get().getNome());
    }

    @Test
    void existsByChaveRespondePresenca() {
        repository.saveAndFlush(permissao("PEDIDOS_CRIAR", "Criar pedido"));

        assertTrue(repository.existsByChave("PEDIDOS_CRIAR"));
        assertFalse(repository.existsByChave("CHAVE_INEXISTENTE"));
    }

    @Test
    void chaveDuplicadaEstouraConstraint() {
        repository.saveAndFlush(permissao("PEDIDOS_CRIAR", "Criar pedido"));

        // saveAndFlush e obrigatorio: sem flush o Hibernate adia o insert e o
        // assertThrows passa sem nunca ter tocado no banco.
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(permissao("PEDIDOS_CRIAR", "Outro rotulo")));
    }

    private Permissao permissao(String chave, String nome) {
        return Permissao.builder()
                .chave(chave)
                .nome(nome)
                .descricao("Permissao de teste")
                .build();
    }
}
