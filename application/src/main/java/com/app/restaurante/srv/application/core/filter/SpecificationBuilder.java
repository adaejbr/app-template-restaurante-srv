package com.app.restaurante.srv.application.core.filter;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Utilitário fluente com métodos auxiliares para construção dinâmica de {@link Specification}.
 *
 * @param <T> tipo da entidade JPA
 */
public class SpecificationBuilder<T> {

    private final List<Specification<T>> specifications = new ArrayList<>();

    public SpecificationBuilder<T> withLike(String property, String value) {
        if (StringUtils.hasText(value)) {
            specifications.add((root, query, cb) -> {
                Path<String> path = root.get(property);
                return cb.like(cb.lower(path), "%" + value.trim().toLowerCase() + "%");
            });
        }
        return this;
    }

    public SpecificationBuilder<T> withEqual(String property, Object value) {
        if (value != null) {
            specifications.add((root, query, cb) -> cb.equal(root.get(property), value));
        }
        return this;
    }

    public SpecificationBuilder<T> withSpecification(Specification<T> spec) {
        if (spec != null) {
            specifications.add(spec);
        }
        return this;
    }

    public Specification<T> build() {
        if (specifications.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (Specification<T> spec : specifications) {
                Predicate predicate = spec.toPredicate(root, query, cb);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
