package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro de consulta paginada para Cliente.
 */
@Getter
@Setter
public class ClienteFilter extends BaseFilter {

    @Schema(description = "Filtro por CPF exato ou parcial", example = "12345678900")
    private String cpf;
}
