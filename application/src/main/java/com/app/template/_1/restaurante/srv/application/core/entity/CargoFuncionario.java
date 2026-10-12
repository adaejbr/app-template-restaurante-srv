package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cargo_funcionario")
@IdClass(CargoFuncionarioId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoFuncionario {

    @Id
    @Column(name = "id_cargo")
    private Integer idCargo;

    @Id
    @Column(name = "id_funcionario")
    private Integer idFuncionario;

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cargo", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_cargofunc_cargo"))
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_cargofunc_funcionario"))
    private Funcionario funcionario;
}