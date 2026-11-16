package com.app.restaurante.srv.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.app.restaurante.srv.application.core.entity.Pedido;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Pedido.
 */
@Schema(description = "Dados do pedido retornado na listagem.")
public record PedidoResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "1")
        Integer comandaId,

        @Schema(example = "1")
        Integer mesaId,

        @Schema(example = "1")
        Integer clienteId,

        @Schema(example = "1")
        Integer funcionarioId,

        @Schema(example = "120.50")
        BigDecimal valorTotal,

        @Schema(example = "CRIADO")
        String status,

        LocalDateTime dataCriacao) {

    public static PedidoResponse from(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        Integer comandaId = pedido.getComanda() != null ? pedido.getComanda().getId() : null;
        Integer mesaId = pedido.getMesa() != null ? pedido.getMesa().getId() : null;
        Integer clienteId = pedido.getCliente() != null ? pedido.getCliente().getId() : null;
        Integer funcionarioId = pedido.getFuncionario() != null ? pedido.getFuncionario().getId() : null;
        return new PedidoResponse(
                pedido.getId(),
                comandaId,
                mesaId,
                clienteId,
                funcionarioId,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao());
    }
}
