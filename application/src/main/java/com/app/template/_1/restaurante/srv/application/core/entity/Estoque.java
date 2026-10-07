package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false, unique = true,
        foreignKey = @ForeignKey(name = "fk_estoque_produto"))
    private Produto produto;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 0;
}