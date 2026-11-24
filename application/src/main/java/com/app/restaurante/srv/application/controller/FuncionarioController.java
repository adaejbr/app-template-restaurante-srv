package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.AlterarSenhaFuncionarioRequest;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.service.FuncionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST de funcionários: listagem paginada e alteração de senha por gerente.
 */
@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    @Operation(summary = "Listar funcionários com busca textual, filtro por status e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de funcionários retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public PageResponse<FuncionarioListResponse> listar(@ModelAttribute FuncionarioFilter filter) {
        return funcionarioService.listar(filter);
    }

    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('FUNCIONARIOS_EDITAR')")
    @Operation(summary = "Gerente define senha temporária de outro funcionário",
            description = "Não exige a senha atual do funcionário alvo. O funcionário fica marcado para "
                    + "trocar a senha no próximo acesso. Não altera a senha do próprio usuário autenticado.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha alterada"),
        @ApiResponse(responseCode = "400", description = "Nova senha ausente ou fraca", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403",
                description = "Sem a permissão FUNCIONARIOS_EDITAR ou tentativa de alterar a própria senha",
                content = @Content),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content)
    })
    public void alterarSenha(@PathVariable Integer id,
            @Valid @RequestBody AlterarSenhaFuncionarioRequest request,
            Authentication authentication) {
        funcionarioService.alterarSenhaPorGerente(id, request.novaSenha(), authentication.getName());
    }
}
