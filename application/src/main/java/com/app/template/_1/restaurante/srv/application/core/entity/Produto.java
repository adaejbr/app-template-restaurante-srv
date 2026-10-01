package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "valor_produto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorProduto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca_produto", nullable = false,
        foreignKey = @ForeignKey(name = "fk_produto_marca"))
    private MarcaProduto marcaProduto;
}