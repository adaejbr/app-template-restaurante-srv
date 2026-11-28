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