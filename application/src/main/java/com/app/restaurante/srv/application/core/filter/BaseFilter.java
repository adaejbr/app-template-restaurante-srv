package com.app.restaurante.srv.application.core.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro base para requisições de listagem com suporte a busca textual, paginação e ordenação.
 */
@Getter
@Setter
public class BaseFilter {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Schema(description = "Termo para busca textual livre", example = "pizza")
    private String q;

    @Schema(description = "Número da página (iniciando em 0)", example = "0", defaultValue = "0")
    private Integer page = DEFAULT_PAGE;

    @Schema(description = "Quantidade de registros por página", example = "10", defaultValue = "10")
    private Integer size = DEFAULT_SIZE;

    @Schema(description = "Critério de ordenação no formato 'campo,direcao'", example = "nome,asc")
    private String sort;

    public Integer getPage() {
        return (page != null && page >= 0) ? page : DEFAULT_PAGE;
    }

    public Integer getSize() {
        return (size != null && size > 0) ? size : DEFAULT_SIZE;
    }
}
