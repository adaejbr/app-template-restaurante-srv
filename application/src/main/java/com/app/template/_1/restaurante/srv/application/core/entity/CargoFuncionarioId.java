package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

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