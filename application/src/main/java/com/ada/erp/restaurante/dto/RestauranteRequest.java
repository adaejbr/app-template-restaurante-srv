package com.ada.erp.restaurante.dto;

import com.ada.erp.shared.validation.CNPJ;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload do PUT do restaurante singleton. O id precisa bater com o do registro
 * configurado, senao a regra de negocio e violada.
 */
public record RestauranteRequest(
        @NotNull Integer id,
        @NotBlank String nome,
        @NotBlank @CNPJ String cnpj,
        String telefone) {
}
