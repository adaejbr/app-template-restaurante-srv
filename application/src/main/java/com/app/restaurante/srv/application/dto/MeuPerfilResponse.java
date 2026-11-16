package com.app.restaurante.srv.application.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída com os dados do perfil e permissões do funcionário autenticado.
 *
 * @param id         identificador do funcionário
 * @param nome       nome completo (via Perfil)
 * @param email      e-mail/login do funcionário
 * @param cpf        CPF do funcionário (somente leitura)
 * @param telefone   telefone de contato
 * @param cargo      cargo ativo do funcionário (somente leitura)
 * @param permissoes lista de permissões/authorities atribuídas
 */
@Schema(description = "Dados do perfil e permissões do usuário autenticado, sem credenciais.")
public record MeuPerfilResponse(
        @Schema(description = "ID do funcionário", example = "1") Integer id,
        @Schema(description = "Nome completo", example = "Gerente Padrao") String nome,
        @Schema(description = "E-mail/login", example = "gerente@restaurante.com") String email,
        @Schema(description = "CPF do funcionário", example = "00000000000",
                accessMode = Schema.AccessMode.READ_ONLY) String cpf,
        @Schema(description = "Telefone de contato", example = "45999999999") String telefone,
        @Schema(description = "Cargo ativo", example = "GERENTE",
                accessMode = Schema.AccessMode.READ_ONLY) String cargo,
        @Schema(description = "Permissões atribuídas ao usuário",
                example = "[\"ROLE_USER\", \"ROLE_MEU_PERFIL_VIEW\"]") List<String> permissoes) {
}
