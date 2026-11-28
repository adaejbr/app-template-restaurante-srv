package com.app.restaurante.srv.application.core.entity;

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