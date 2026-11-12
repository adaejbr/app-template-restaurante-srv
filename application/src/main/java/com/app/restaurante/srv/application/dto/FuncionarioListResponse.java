package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.entity.Funcionario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Funcionario (sem dados de senha).
 */
@Schema(description = "Dados do funcionário retornado na listagem.")
public record FuncionarioListResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "gerente@restaurante.com")
        String login,

        @Schema(example = "Gerente Padrão")
        String nome,

        @Schema(example = "(45) 99999-9999")
        String telefone,

        @Schema(example = "ATIVO")
        String status) {

    public static FuncionarioListResponse from(Funcionario funcionario) {
        if (funcionario == null) {
            return null;
        }
        String nome = funcionario.getPerfil() != null ? funcionario.getPerfil().getNome() : null;
        String telefone = funcionario.getPerfil() != null ? funcionario.getPerfil().getTelefone() : null;
        return new FuncionarioListResponse(
                funcionario.getId(),
                funcionario.getLogin(),
                nome,
                telefone,
                funcionario.getStatus());
    }
}
