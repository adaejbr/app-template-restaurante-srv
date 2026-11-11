package com.app.restaurante.srv.application.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoFuncionarioId implements Serializable {

    @Column(name = "id_cargo")
    private Integer idCargo;

    @Column(name = "id_funcionario")
    private Integer idFuncionario;
}