package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;
}