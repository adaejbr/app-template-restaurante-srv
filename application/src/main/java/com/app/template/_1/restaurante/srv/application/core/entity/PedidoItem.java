package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_item")
@IdClass(PedidoItemId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItem {

    @Id
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Id
    @Column(name = "id_produto")
    private Integer idProduto;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    @Column(name = "valor_pedido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_pedidoitem_pedido"))
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_pedidoitem_produto"))
    private Produto produto;
}