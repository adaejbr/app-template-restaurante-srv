package com.app.template._1.restaurante.srv.application.infra.repository;

import com.app.template._1.restaurante.srv.application.core.entity.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetorRepository extends JpaRepository<Setor, Integer> {
}