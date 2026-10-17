package com.app.template._1.restaurante.srv.application.infra.repository;

import com.app.template._1.restaurante.srv.application.core.entity.ProdutoFornecedor;
import com.app.template._1.restaurante.srv.application.core.entity.ProdutoFornecedorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedor, ProdutoFornecedorId> {
}