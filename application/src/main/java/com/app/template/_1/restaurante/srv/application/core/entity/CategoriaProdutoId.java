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
public class CategoriaProdutoId implements Serializable {

    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "id_produto")
    private Integer idProduto;
}