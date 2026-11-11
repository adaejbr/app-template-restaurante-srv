package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.entity.Mesa;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Mesa.
 */
@Schema(description = "Dados da mesa retornada na listagem.")
public record MesaResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "Mesa 01")
        String identificacao,

        @Schema(example = "Salão")
        String area,

        @Schema(example = "LIVRE")
        String status,

        @Schema(example = "1")
        Integer restauranteId) {

    public static MesaResponse from(Mesa mesa) {
        if (mesa == null) {
            return null;
        }
        Integer restId = mesa.getRestaurante() != null ? mesa.getRestaurante().getId() : null;
        return new MesaResponse(mesa.getId(), mesa.getIdentificacao(), mesa.getArea(), mesa.getStatus(), restId);
    }
}
