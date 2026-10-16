package com.app.template._1.restaurante.srv.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para autenticação (task j-05).
 *
 * @param email e-mail/login do funcionário
 * @param senha senha em texto plano
 */
public record LoginRequest(
        @NotBlank @Email @Schema(example = "gerente@restaurante.com") String email,
        @NotBlank @Schema(example = "admin123", accessMode = Schema.AccessMode.WRITE_ONLY) String senha) {

    @Override
    public String toString() {
        return "LoginRequest[credentials=REDACTED]";
    }
}
