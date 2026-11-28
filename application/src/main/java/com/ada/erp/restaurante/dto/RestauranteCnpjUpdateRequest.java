package com.ada.erp.restaurante.dto;

import com.ada.erp.shared.validation.CNPJ;
import jakarta.validation.constraints.NotBlank;

public record RestauranteCnpjUpdateRequest(@NotBlank @CNPJ String cnpj) {
}
