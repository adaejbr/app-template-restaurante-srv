package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.FuncionarioCreateRequest;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.dto.FuncionarioResponse;
import com.app.restaurante.srv.application.dto.FuncionarioUpdateRequest;
import com.app.restaurante.srv.application.service.FuncionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para listagem paginada e manutenção de funcionários.
 */
@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FUNCIONARIOS_CRIAR')")
    @Operation(summary = "Cadastrar funcionário")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Funcionário cadastrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para cadastrar"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado"),
        @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado")
    })
    public FuncionarioResponse criar(@Valid @RequestBody FuncionarioCreateRequest request) {
        return funcionarioService.criar(request);
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

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar funcionário com senha opcional",
            description = "Atualiza dados pessoais, login, cargo e opcionalmente a senha. "
                    + "Quando a senha não é informada ou está em branco, o hash atual é preservado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Payload inválido"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Funcionário ou cargo não encontrado"),
        @ApiResponse(responseCode = "409", description = "CPF ou login já cadastrado para outro funcionário")
    })
    public FuncionarioResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody FuncionarioUpdateRequest request) {
        return funcionarioService.atualizar(id, request);
    }
}
