package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.Cliente;
import com.app.restaurante.srv.application.core.entity.Perfil;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.ClienteFilter;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Specification dinâmica para {@link Cliente}.
 */
public final class ClienteSpecification {

    private ClienteSpecification() {
    }

    public static Specification<Cliente> from(ClienteFilter filter) {
        SpecificationBuilder<Cliente> builder = new SpecificationBuilder<>();
        if (filter == null) {
            return builder.build();
        }

        if (StringUtils.hasText(filter.getQ())) {
            String termo = filter.getQ().trim().toLowerCase();
            builder.withSpecification((root, query, cb) -> {
                Join<Cliente, Perfil> perfilJoin = root.join("perfil");
                return cb.or(
                        cb.like(cb.lower(perfilJoin.get("nome")), "%" + termo + "%"),
                        cb.like(perfilJoin.get("cpf"), "%" + termo + "%"),
                        cb.like(perfilJoin.get("telefone"), "%" + termo + "%"));
            });
        }

        if (StringUtils.hasText(filter.getCpf())) {
            String cpf = filter.getCpf().trim();
            builder.withSpecification((root, query, cb) -> {
                Join<Cliente, Perfil> perfilJoin = root.join("perfil");
                return cb.like(perfilJoin.get("cpf"), "%" + cpf + "%");
            });
        }

        return builder.build();
    }
}
