package com.ada.erp.cargo.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;

/**
 * Payload do PUT de permissoes do cargo. A lista substitui o conjunto inteiro;
 * vazia remove todos os vinculos ativos.
 */
public record CargoPermissaoUpdateRequest(
        @NotNull List<@NotNull Integer> permissoes) {
}
