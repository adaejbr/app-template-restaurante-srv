package com.app.template._1.restaurante.srv.application.core.entity;

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
public class PermissaoCargoId implements Serializable {

    @Column(name = "id_permissao")
    private Integer idPermissao;

    @Column(name = "id_cargo")
    private Integer idCargo;
}