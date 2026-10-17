package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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