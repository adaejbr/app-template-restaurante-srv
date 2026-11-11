package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.EstoqueFilter;
import com.app.restaurante.srv.application.dto.EstoqueResponse;
import com.app.restaurante.srv.application.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para listagem paginada de estoque.
 */
@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    @Operation(summary = "Listar itens de estoque com busca textual por produto, filtros e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de estoque retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public PageResponse<EstoqueResponse> listar(@ModelAttribute EstoqueFilter filter) {
        return estoqueService.listar(filter);
    }
}
