package com.app.restaurante.srv.application.controller;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.dto.ClienteFilter;
import com.app.restaurante.srv.application.dto.ClienteResponse;
import com.app.restaurante.srv.application.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para listagem paginada de clientes.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar clientes com busca textual, filtro e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de clientes retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public PageResponse<ClienteResponse> listar(@ModelAttribute ClienteFilter filter) {
        return clienteService.listar(filter);
    }
}
