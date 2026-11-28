package com.app.restaurante.srv.application.dto;

import com.ada.erp.shared.validation.SenhaForte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record FuncionarioCreateRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @CPF(message = "CPF inválido")
        @Pattern(regexp = "[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}") String cpf,
        @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 50) String email,
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotNull @SenhaForte String senha,
        @NotNull @Positive Integer enderecoId) {

    @Override
    public String toString() {
        return "FuncionarioCreateRequest";
    }
}
