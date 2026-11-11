package com.app.restaurante.srv.application.infra.repository;

import com.app.restaurante.srv.application.core.entity.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {
    Estoque findByProdutoId(Integer produtoId);
}