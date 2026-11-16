package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.ProdutoFilter;
import com.app.restaurante.srv.application.dto.ProdutoResponse;
import com.app.restaurante.srv.application.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para consulta e listagem paginada de produtos.
 */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    @Operation(summary = "Listar produtos com busca textual, filtro por categoria e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de produtos retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public PageResponse<ProdutoResponse> listar(@ModelAttribute ProdutoFilter filter) {
        return produtoService.listar(filter);
    }
}
