package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro de consulta paginada para Estoque.
 */
@Getter
@Setter
public class EstoqueFilter extends BaseFilter {

    @Schema(description = "Identificador específico do produto", example = "1")
    private Integer produtoId;

    @Schema(description = "Filtrar por estoque máximo (ex: trazer itens abaixo de um limite para reposição)", example = "5")
    private Integer quantidadeMax;
}
