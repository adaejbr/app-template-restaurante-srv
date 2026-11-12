package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.entity.Comanda;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Comanda.
 */
@Schema(description = "Dados da comanda retornada na listagem.")
public record ComandaResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "COM-101")
        String identificacao,

        @Schema(example = "ABERTA")
        String status,

        @Schema(example = "1")
        Integer restauranteId) {

    public static ComandaResponse from(Comanda comanda) {
        if (comanda == null) {
            return null;
        }
        Integer restId = comanda.getRestaurante() != null ? comanda.getRestaurante().getId() : null;
        return new ComandaResponse(comanda.getId(), comanda.getIdentificacao(), comanda.getStatus(), restId);
    }
}
