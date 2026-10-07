package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cidade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false,
        foreignKey = @ForeignKey(name = "fk_cidade_estado"))
    private Estado estado;
}