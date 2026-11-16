package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro específico para consulta e paginação de produtos.
 */
@Getter
@Setter
public class ProdutoFilter extends BaseFilter {

    @Schema(description = "Identificador da categoria para filtrar produtos", example = "1")
    private Integer categoriaId;
}
