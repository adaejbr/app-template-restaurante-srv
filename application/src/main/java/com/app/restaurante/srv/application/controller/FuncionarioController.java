package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.service.FuncionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para listagem paginada de funcionários.
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
}
