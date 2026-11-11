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
public class ProdutoFornecedorId implements Serializable {

    @Column(name = "id_produto")
    private Integer idProduto;

    @Column(name = "id_fornecedor")
    private Integer idFornecedor;
}