package com.app.restaurante.srv.application.core.filter;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

/**
 * Envelope padronizado para respostas paginadas.
 *
 * @param <T> tipo dos elementos contidos na página
 */
@Schema(description = "Estrutura padronizada de resposta paginada.")
public record PageResponse<T>(
        @Schema(description = "Lista dos elementos da página atual")
        List<T> content,

        @Schema(description = "Índice da página atual (base zero)", example = "0")
        int page,

        @Schema(description = "Quantidade de registros por página", example = "10")
        int size,

        @Schema(description = "Total de registros encontrados considerando os filtros", example = "42")
        long totalElements,

        @Schema(description = "Total de páginas disponíveis", example = "5")
        int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
