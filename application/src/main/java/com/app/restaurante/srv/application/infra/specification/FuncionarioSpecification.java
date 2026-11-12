package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.core.entity.Perfil;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Specification dinâmica para {@link Funcionario}.
 */
public final class FuncionarioSpecification {

    private FuncionarioSpecification() {
    }

    public static Specification<Funcionario> from(FuncionarioFilter filter) {
        SpecificationBuilder<Funcionario> builder = new SpecificationBuilder<>();
        if (filter == null) {
            return builder.build();
        }

        if (StringUtils.hasText(filter.getQ())) {
            String termo = filter.getQ().trim().toLowerCase();
            builder.withSpecification((root, query, cb) -> {
                Join<Funcionario, Perfil> perfilJoin = root.join("perfil");
                return cb.or(
                        cb.like(cb.lower(root.get("login")), "%" + termo + "%"),
                        cb.like(cb.lower(perfilJoin.get("nome")), "%" + termo + "%"),
                        cb.like(perfilJoin.get("cpf"), "%" + termo + "%"));
            });
        }

        builder.withEqual("status", filter.getStatus());

        return builder.build();
    }
}
