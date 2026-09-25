package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissaoCargoId implements Serializable {

    @Column(name = "id_permissao")
    private Integer idPermissao;

    @Column(name = "id_cargo")
    private Integer idCargo;
}