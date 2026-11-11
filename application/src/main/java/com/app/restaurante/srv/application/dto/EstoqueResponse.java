package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.entity.Estoque;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Estoque.
 */
@Schema(description = "Dados do estoque retornado na listagem.")
public record EstoqueResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "1")
        Integer produtoId,

        @Schema(example = "Refrigerante Cola 350ml")
        String produtoNome,

        @Schema(example = "24")
        Integer quantidade) {

    public static EstoqueResponse from(Estoque estoque) {
        if (estoque == null) {
            return null;
        }
        Integer produtoId = estoque.getProduto() != null ? estoque.getProduto().getId() : null;
        String produtoNome = estoque.getProduto() != null ? estoque.getProduto().getNome() : null;
        return new EstoqueResponse(estoque.getId(), produtoId, produtoNome, estoque.getQuantidade());
    }
}
