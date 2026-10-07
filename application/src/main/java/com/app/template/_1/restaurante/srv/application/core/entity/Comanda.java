package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comanda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "identificacao", nullable = false, length = 50)
    private String identificacao;

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_restaurante", nullable = false,
        foreignKey = @ForeignKey(name = "fk_comanda_restaurante"))
    private Restaurante restaurante;
}