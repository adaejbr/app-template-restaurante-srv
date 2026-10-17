package com.app.template._1.restaurante.srv.application.infra.repository;

import com.app.template._1.restaurante.srv.application.core.entity.PedidoItem;
import com.app.template._1.restaurante.srv.application.core.entity.PedidoItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, PedidoItemId> {
}