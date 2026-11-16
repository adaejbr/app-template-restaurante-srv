package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro de consulta paginada para Mesa.
 */
@Getter
@Setter
public class MesaFilter extends BaseFilter {

    @Schema(description = "Status da mesa (ex: LIVRE, OCUPADA, RESERVADA)", example = "LIVRE")
    private String status;

    @Schema(description = "Área ou setor físico da mesa (ex: Salão, Varanda)", example = "Salão")
    private String area;

    @Schema(description = "Identificador do restaurante", example = "1")
    private Integer restauranteId;
}
