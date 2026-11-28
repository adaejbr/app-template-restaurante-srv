package com.app.restaurante.srv.application.infra.repository;

import java.util.Optional;
import com.app.restaurante.srv.application.core.entity.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Integer>,
        JpaSpecificationExecutor<Permissao> {

    // Mantido por compatibilidade com o que ja existia.
    Permissao findByNome(String nome);

    // Optional porque a chave pode nao existir.
    Optional<Permissao> findByChave(String chave);

    // Usado pela PERM-02 pra responder 409 antes de tentar o insert.
    boolean existsByChave(String chave);
}
