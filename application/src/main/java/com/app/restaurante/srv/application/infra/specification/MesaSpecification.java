package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.Mesa;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.MesaFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification dinâmica para {@link Mesa}.
 */
public final class MesaSpecification {

    private MesaSpecification() {
    }

    public static Specification<Mesa> from(MesaFilter filter) {
        SpecificationBuilder<Mesa> builder = new SpecificationBuilder<>();
        if (filter == null) {
            return builder.build();
        }

        builder.withLike("identificacao", filter.getQ());
        builder.withEqual("status", filter.getStatus());
        builder.withEqual("area", filter.getArea());

        if (filter.getRestauranteId() != null) {
            Integer restId = filter.getRestauranteId();
            builder.withSpecification((root, query, cb) -> cb.equal(root.get("restaurante").get("id"), restId));
        }

        return builder.build();
    }
}
