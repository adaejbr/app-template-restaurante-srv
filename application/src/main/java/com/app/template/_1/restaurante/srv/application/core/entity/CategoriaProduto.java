package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria_produto")
@IdClass(CategoriaProdutoId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaProduto {

    @Id
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Id
    @Column(name = "id_produto")
    private Integer idProduto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_categoriaproduto_categoria"))
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_categoriaproduto_produto"))
    private Produto produto;
}