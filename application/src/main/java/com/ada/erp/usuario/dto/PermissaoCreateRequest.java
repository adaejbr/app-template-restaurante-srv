package com.ada.erp.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrato de criacao; a chave e imutavel depois de gravada. */
public record PermissaoCreateRequest(
        @NotBlank @Size(max = 100) String chave,
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 255) String descricao) {
}
