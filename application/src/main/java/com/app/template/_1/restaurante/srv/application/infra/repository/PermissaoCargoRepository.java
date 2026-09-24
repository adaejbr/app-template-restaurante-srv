package com.app.template._1.restaurante.srv.application.infra.repository;

import com.app.template._1.restaurante.srv.application.core.entity.PermissaoCargo;
import com.app.template._1.restaurante.srv.application.core.entity.PermissaoCargoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissaoCargoRepository extends JpaRepository<PermissaoCargo, PermissaoCargoId> {
}