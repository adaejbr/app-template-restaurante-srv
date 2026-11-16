package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.CategoriaProduto;
import com.app.restaurante.srv.application.core.entity.Produto;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.ProdutoFilter;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/**
 * Construtor de {@link Specification} para filtragem dinâmica da entidade {@link Produto}.
 */
public final class ProdutoSpecification {

    private ProdutoSpecification() {
    }

    /**
     * Monta a {@link Specification} de {@link Produto} a partir dos parâmetros do {@link ProdutoFilter}.
     *
     * @param filter parâmetros de busca textual e filtro por categoria
     * @return {@link Specification} combinada
     */
    public static Specification<Produto> from(ProdutoFilter filter) {
        SpecificationBuilder<Produto> builder = new SpecificationBuilder<>();

        if (filter == null) {
            return builder.build();
        }

        builder.withLike("nome", filter.getQ());

        if (filter.getCategoriaId() != null) {
            Integer categoriaId = filter.getCategoriaId();
            builder.withSpecification((root, query, cb) -> {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<CategoriaProduto> cpRoot = subquery.from(CategoriaProduto.class);
                subquery.select(cpRoot.get("idProduto"))
                        .where(
                                cb.equal(cpRoot.get("idProduto"), root.get("id")),
                                cb.equal(cpRoot.get("idCategoria"), categoriaId));
                return cb.exists(subquery);
            });
        }

        return builder.build();
    }
}
