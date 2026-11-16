package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.Comanda;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.ComandaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification dinâmica para {@link Comanda}.
 */
public final class ComandaSpecification {

    private ComandaSpecification() {
    }

    public static Specification<Comanda> from(ComandaFilter filter) {
        SpecificationBuilder<Comanda> builder = new SpecificationBuilder<>();
        if (filter == null) {
            return builder.build();
        }

        builder.withLike("identificacao", filter.getQ());
        builder.withEqual("status", filter.getStatus());

        if (filter.getRestauranteId() != null) {
            Integer restId = filter.getRestauranteId();
            builder.withSpecification((root, query, cb) -> cb.equal(root.get("restaurante").get("id"), restId));
        }

        return builder.build();
    }
}
