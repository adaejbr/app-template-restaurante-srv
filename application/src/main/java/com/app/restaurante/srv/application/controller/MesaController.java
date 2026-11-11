package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.MesaFilter;
import com.app.restaurante.srv.application.dto.MesaResponse;
import com.app.restaurante.srv.application.service.MesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para listagem paginada de mesas.
 */
@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    @Operation(summary = "Listar mesas com busca textual, filtros de status/área e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de mesas retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public PageResponse<MesaResponse> listar(@ModelAttribute MesaFilter filter) {
        return mesaService.listar(filter);
    }
}
