package com.app.restaurante.srv.application.dto;

import com.ada.erp.shared.validation.SenhaForte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Payload da alteracao de senha por gerente (FUNC-05, #67). Nao ha senha atual:
 * quem altera e o gerente, que nao conhece a senha do funcionario.
 *
 * @param novaSenha senha temporaria; o funcionario deve troca-la no proximo acesso
 */
@Schema(description = "Nova senha definida pelo gerente para o funcionário.")
public record AlterarSenhaFuncionarioRequest(
        @Schema(description = "Senha temporária: 8 a 64 caracteres, até 72 bytes, não pode ser comum",
                example = "Cozinha-Temporaria-2026", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotNull @SenhaForte String novaSenha) {

    /** Evita que a senha apareca em logs que imprimam o record. */
    @Override
    public String toString() {
        return "AlterarSenhaFuncionarioRequest[novaSenha=***]";
    }
}
