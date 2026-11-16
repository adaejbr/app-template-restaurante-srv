package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.ComandaFilter;
import com.app.restaurante.srv.application.dto.ComandaResponse;
import com.app.restaurante.srv.application.service.ComandaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para listagem paginada de comandas.
 */
@RestController
@RequestMapping("/api/comandas")
public class ComandaController {

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping
    @Operation(summary = "Listar comandas com busca textual, filtros e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de comandas retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public PageResponse<ComandaResponse> listar(@ModelAttribute ComandaFilter filter) {
        return comandaService.listar(filter);
    }
}
