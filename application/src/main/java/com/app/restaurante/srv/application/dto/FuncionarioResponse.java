package com.app.restaurante.srv.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta detalhada de Funcionario (sem dados de senha).
 */
@Schema(description = "Dados do funcionário retornado após operações de escrita/detalhe.")
public record FuncionarioResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "maria@restaurante.com")
        String login,

        @Schema(example = "Maria Silva")
        String nome,

        @Schema(example = "123.456.789-01")
        String cpf,

        @Schema(example = "(45) 99999-9999")
        String telefone,

        @Schema(example = "ATIVO")
        String status,

        @Schema(example = "1")
        Integer idCargo,

        @Schema(example = "Gerente")
        String nomeCargo) {
}
