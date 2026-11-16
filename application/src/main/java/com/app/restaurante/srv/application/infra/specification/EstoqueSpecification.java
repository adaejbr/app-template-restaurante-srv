package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.Estoque;
import com.app.restaurante.srv.application.core.entity.Produto;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.EstoqueFilter;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Specification dinâmica para {@link Estoque}.
 */
public final class EstoqueSpecification {

    private EstoqueSpecification() {
    }

    public static Specification<Estoque> from(EstoqueFilter filter) {
        SpecificationBuilder<Estoque> builder = new SpecificationBuilder<>();
        if (filter == null) {
            return builder.build();
        }

        if (StringUtils.hasText(filter.getQ())) {
            String termo = filter.getQ().trim().toLowerCase();
            builder.withSpecification((root, query, cb) -> {
                Join<Estoque, Produto> produtoJoin = root.join("produto");
                return cb.like(cb.lower(produtoJoin.get("nome")), "%" + termo + "%");
            });
        }

        if (filter.getProdutoId() != null) {
            Integer produtoId = filter.getProdutoId();
            builder.withSpecification((root, query, cb) -> cb.equal(root.get("produto").get("id"), produtoId));
        }

        if (filter.getQuantidadeMax() != null) {
            Integer qtdMax = filter.getQuantidadeMax();
            builder.withSpecification((root, query, cb) -> cb.lessThanOrEqualTo(root.get("quantidade"), qtdMax));
        }

        return builder.build();
    }
}
