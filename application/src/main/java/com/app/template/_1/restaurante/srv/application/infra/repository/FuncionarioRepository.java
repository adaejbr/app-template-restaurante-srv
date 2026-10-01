package com.app.template._1.restaurante.srv.application.infra.repository;

import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {
    Funcionario findByLogin(String login);

    /**
     * CA da issue #7: método findByEmail.
     * A tabela funcionario nao possui coluna `email` (o e-mail e armazenado na
     * coluna `login`), entao este metodo apenas delega para findByLogin.
     * Como e um metodo `default` com corpo, o Spring Data nao tenta derivar
     * query JPQL a partir do nome.
     */
    default Funcionario findByEmail(String email) {
        return findByLogin(email);
    }
}