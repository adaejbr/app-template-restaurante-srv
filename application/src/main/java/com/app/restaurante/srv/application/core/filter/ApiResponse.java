package com.app.restaurante.srv.application.core.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * Envelope padronizado para respostas de negocio (INFRA-02).
 *
 * @param <T> tipo dos dados da resposta
 */
@Schema(description = "Estrutura padronizada de resposta de API.")
public record ApiResponse<T>(
        @Schema(description = "Dados da resposta")
        T data,

        @Schema(description = "Mensagem descritiva", example = "Operacao realizada com sucesso")
        String message,

        @Schema(description = "Instanto da resposta")
        Instant timestamp,

        @Schema(description = "Caminho da requisicao")
        String path,

        @Schema(description = "Problemas de validacao, quando houver")
        List<FieldErrorDTO> errors) {

    public static <T> ApiResponse<T> of(T data, String message, String path) {
        return new ApiResponse<>(data, message, Instant.now(), path, null);
    }

    public static <T> ApiResponse<T> success(T data, String path) {
        return of(data, "Operacao realizada com sucesso", path);
    }

    public static <T> ApiResponse<T> failure(String message, String path, List<FieldErrorDTO> errors) {
        return new ApiResponse<>(null, message, Instant.now(), path, errors);
    }

    public record FieldErrorDTO(String field, String message, Object rejectedValue) {
    }
}
