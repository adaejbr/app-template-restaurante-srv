package com.app.restaurante.srv.application.dto;

import java.util.Comparator;
import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Funcionario (sem dados de senha).
 */
@Schema(description = "Dados do funcionário retornado na listagem.")
public record FuncionarioListResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "Gerente Padrão")
        String nome,

        @Schema(example = "12345678900")
        String cpf,

        @Schema(example = "gerente@restaurante.com")
        String email,

        @Schema(example = "1")
        Integer cargoId,

        @Schema(example = "GERENTE")
        String cargoNome,

        @Schema(example = "ATIVO")
        String status) {

    public static FuncionarioListResponse from(Funcionario funcionario) {
        if (funcionario == null) {
            return null;
        }
        String nome = funcionario.getPerfil() != null ? funcionario.getPerfil().getNome() : null;
        String cpf = funcionario.getPerfil() != null ? funcionario.getPerfil().getCpf() : null;
        CargoFuncionario cargoAtivo = funcionario.getCargos() != null
                ? funcionario.getCargos().stream()
                        .filter(cf -> "ATIVO".equalsIgnoreCase(cf.getStatus()))
                        .min(Comparator.comparing(
                                CargoFuncionario::getIdCargo,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(null)
                : null;
        Integer cargoId = cargoAtivo != null ? cargoAtivo.getIdCargo() : null;
        String cargoNome = (cargoAtivo != null && cargoAtivo.getCargo() != null)
                ? cargoAtivo.getCargo().getNome()
                : null;
        return new FuncionarioListResponse(
                funcionario.getId(),
                nome,
                cpf,
                funcionario.getLogin(),
                cargoId,
                cargoNome,
                funcionario.getStatus());
    }
}
