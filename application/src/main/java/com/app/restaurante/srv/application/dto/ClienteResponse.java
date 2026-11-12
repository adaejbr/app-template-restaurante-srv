package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.entity.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Cliente.
 */
@Schema(description = "Dados do cliente retornado.")
public record ClienteResponse(
        @Schema(example = "1")
        Integer id,

        @Schema(example = "João Silva")
        String nome,

        @Schema(example = "12345678900")
        String cpf,

        @Schema(example = "(45) 99999-9999")
        String telefone) {

    public static ClienteResponse from(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        String nome = cliente.getPerfil() != null ? cliente.getPerfil().getNome() : null;
        String cpf = cliente.getPerfil() != null ? cliente.getPerfil().getCpf() : null;
        String telefone = cliente.getPerfil() != null ? cliente.getPerfil().getTelefone() : null;
        return new ClienteResponse(cliente.getId(), nome, cpf, telefone);
    }
}
