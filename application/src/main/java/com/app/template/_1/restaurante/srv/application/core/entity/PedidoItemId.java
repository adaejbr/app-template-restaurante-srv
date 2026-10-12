package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItemId implements Serializable {

    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "id_produto")
    private Integer idProduto;
}