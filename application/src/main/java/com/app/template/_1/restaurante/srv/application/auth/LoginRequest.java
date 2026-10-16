package com.app.template._1.restaurante.srv.application.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Schema(description = "Nome de usuário", example = "user") String username,
        @NotBlank @Schema(description = "Senha configurada ou gerada na inicialização",
                example = "substitua-pela-sua-senha", accessMode = Schema.AccessMode.WRITE_ONLY) String password) {

    @Override
    public String toString() {
        return "LoginRequest[credentials=REDACTED]";
    }
}
