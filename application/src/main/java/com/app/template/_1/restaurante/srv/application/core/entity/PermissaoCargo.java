package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissao_cargo")
@IdClass(PermissaoCargoId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissaoCargo {

    @Id
    @Column(name = "id_permissao")
    private Integer idPermissao;

    @Id
    @Column(name = "id_cargo")
    private Integer idCargo;

    @Column(name = "situacao", nullable = false, length = 30)
    private String situacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permissao", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_permcargo_permissao"))
    private Permissao permissao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cargo", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_permcargo_cargo"))
    private Cargo cargo;
}