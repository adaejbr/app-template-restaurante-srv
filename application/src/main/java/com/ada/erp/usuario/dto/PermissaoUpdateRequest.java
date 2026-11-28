package com.ada.erp.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contrato de atualizacao. "chave" e aceita apenas para detectar tentativa de
 * alteracao e responder 422; o valor gravado nunca muda.
 */
public record PermissaoUpdateRequest(
        @Size(max = 100) String chave,
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 255) String descricao) {
}
