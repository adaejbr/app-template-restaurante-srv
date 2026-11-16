package com.ada.erp.usuario.specification;

import com.app.restaurante.srv.application.core.entity.Permissao;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** Filtro textual (q) sobre chave e nome, ate a infraestrutura do BUSCA-01. */
public final class PermissaoSpecification {

    private PermissaoSpecification() {
    }

    public static Specification<Permissao> comBusca(String q) {
        if (!StringUtils.hasText(q)) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        String termo = "%" + q.trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("chave")), termo),
                criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("nome")), termo));
    }
}
