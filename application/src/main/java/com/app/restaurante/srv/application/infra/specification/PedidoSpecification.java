package com.app.restaurante.srv.application.infra.specification;

import com.app.restaurante.srv.application.core.entity.Pedido;
import com.app.restaurante.srv.application.core.filter.SpecificationBuilder;
import com.app.restaurante.srv.application.dto.PedidoFilter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification dinâmica para {@link Pedido}.
 */
public final class PedidoSpecification {

    private PedidoSpecification() {
    }

    public static Specification<Pedido> from(PedidoFilter filter) {
        SpecificationBuilder<Pedido> builder = new SpecificationBuilder<>();
        if (filter == null) {
            return builder.build();
        }

        builder.withLike("status", filter.getQ());
        builder.withEqual("status", filter.getStatus());

        if (filter.getComandaId() != null) {
            Integer comandaId = filter.getComandaId();
            builder.withSpecification((root, query, cb) -> cb.equal(root.get("comanda").get("id"), comandaId));
        }

        if (filter.getMesaId() != null) {
            Integer mesaId = filter.getMesaId();
            builder.withSpecification((root, query, cb) -> cb.equal(root.get("mesa").get("id"), mesaId));
        }

        if (filter.getClienteId() != null) {
            Integer clienteId = filter.getClienteId();
            builder.withSpecification((root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId));
        }

        if (filter.getFuncionarioId() != null) {
            Integer funcionarioId = filter.getFuncionarioId();
            builder.withSpecification((root, query, cb) -> cb.equal(root.get("funcionario").get("id"), funcionarioId));
        }

        return builder.build();
    }
}
