package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "produto_fornecedor")
@IdClass(ProdutoFornecedorId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoFornecedor {

    @Id
    @Column(name = "id_produto")
    private Integer idProduto;

    @Id
    @Column(name = "id_fornecedor")
    private Integer idFornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_produtofornecedor_produto"))
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fornecedor", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_produtofornecedor_fornecedor"))
    private Fornecedor fornecedor;
}