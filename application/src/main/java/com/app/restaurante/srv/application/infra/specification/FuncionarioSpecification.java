package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.core.entity.Perfil;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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

        if (filter.getCargoId() != null) {
            Integer cargoId = filter.getCargoId();
            builder.withSpecification((root, query, cb) -> {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<CargoFuncionario> cfRoot = subquery.from(CargoFuncionario.class);
                subquery.select(cfRoot.get("idFuncionario"))
                        .where(
                                cb.equal(cfRoot.get("idFuncionario"), root.get("id")),
                                cb.equal(cfRoot.get("idCargo"), cargoId),
                                cb.equal(cfRoot.get("status"), "ATIVO"));
                return cb.exists(subquery);
            });
        }

        builder.withEqual("status", filter.getStatus());

        return builder.build();
    }
}
