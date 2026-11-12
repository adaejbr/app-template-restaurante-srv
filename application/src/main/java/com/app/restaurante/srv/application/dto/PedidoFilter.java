package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro de consulta paginada para Pedido.
 */
@Getter
@Setter
public class PedidoFilter extends BaseFilter {

    @Schema(description = "Status do pedido (ex: CRIADO, PREPARANDO, ENTREGUE, CANCELADO)", example = "CRIADO")
    private String status;

    @Schema(description = "Identificador da comanda associada", example = "1")
    private Integer comandaId;

    @Schema(description = "Identificador da mesa associada", example = "1")
    private Integer mesaId;

    @Schema(description = "Identificador do cliente", example = "1")
    private Integer clienteId;

    @Schema(description = "Identificador do funcionário que registrou", example = "1")
    private Integer funcionarioId;
}
