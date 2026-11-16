package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro de consulta paginada para Comanda.
 */
@Getter
@Setter
public class ComandaFilter extends BaseFilter {

    @Schema(description = "Status da comanda (ex: ABERTA, FECHADA, CANCELADA)", example = "ABERTA")
    private String status;

    @Schema(description = "Identificador do restaurante", example = "1")
    private Integer restauranteId;
}
