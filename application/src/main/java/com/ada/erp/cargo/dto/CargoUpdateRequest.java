package com.ada.erp.cargo.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Contrato de atualizacao de cargo e sincronizacao opcional de permissoes. */
public record CargoUpdateRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotNull Integer idSetor,
        List<@NotNull Integer> permissoes) {
}
