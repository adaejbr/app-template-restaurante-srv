package com.app.restaurante.srv.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload de atualização de funcionário (FUNC-03, #70).
 * O campo {@code senha} é opcional: quando ausente ou em branco, mantém o hash atual.
 */
@Schema(description = "Dados para atualização de um funcionário.")
public record FuncionarioUpdateRequest(
        @Schema(example = "Maria Silva")
        @NotBlank
        @Size(max = 150)
        String nome,

        @Schema(example = "123.456.789-01")
        @NotBlank
        @Size(max = 14)
        String cpf,

        @Schema(example = "(45) 99999-9999")
        @Size(max = 20)
        String telefone,

        @Schema(example = "maria@restaurante.com")
        @NotBlank
        @Email
        @Size(max = 50)
        String login,

        @Schema(description = "Nova senha (opcional). Se omitida ou vazia, preserva a senha atual.",
                example = "novaSenha123")
        @Size(max = 100)
        String senha,

        @Schema(example = "1")
        @NotNull
        Integer idCargo,

        @Schema(example = "ATIVO")
        @Size(max = 30)
        String status) {
}
