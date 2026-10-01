package com.app.template._1.restaurante.srv.application.infra.repository;

import com.app.template._1.restaurante.srv.application.core.entity.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Integer> {
}