package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

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