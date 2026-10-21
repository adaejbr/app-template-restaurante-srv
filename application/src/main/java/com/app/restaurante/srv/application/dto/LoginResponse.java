package com.app.restaurante.srv.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída do login (task j-05).
 *
 * @param token  token JWT gerado
 * @param userId id do funcionário autenticado
 * @param nome   nome completo (via Perfil)
 * @param cargo  cargo ativo do funcionário
 */
@Schema(description = "Token de acesso e dados básicos do funcionário autenticado.")
public record LoginResponse(
        @Schema(description = "JWT assinado com HS256") String token,
        @Schema(example = "1") Integer userId,
        @Schema(example = "Gerente Padrao") String nome,
        @Schema(example = "GERENTE") String cargo) {
}
