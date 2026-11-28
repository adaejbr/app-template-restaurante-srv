package com.ada.erp.restaurante.controller;

import com.ada.erp.restaurante.dto.RestauranteCnpjResponse;
import com.ada.erp.restaurante.dto.RestauranteCnpjUpdateRequest;
import com.ada.erp.restaurante.dto.RestauranteRequest;
import com.ada.erp.restaurante.dto.RestauranteResponse;
import com.ada.erp.restaurante.service.RestauranteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Restaurante singleton: existe um unico registro, configurado pelo seed e
 * atualizado no lugar. Nao ha POST, porque a API nao cria restaurante.
 */
@Tag(name = "Restaurante", description = "Restaurante singleton: um unico registro, sem POST")
@RestController
public class RestauranteController {
    private final RestauranteService service;

    public RestauranteController(RestauranteService service) {
        this.service = service;
    }

    @GetMapping("/api/restaurante")
    @PreAuthorize("hasAuthority('RESTAURANTE_VISUALIZAR')")
    @Operation(summary = "Exibe o restaurante configurado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante configurado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão de visualizar"),
        @ApiResponse(responseCode = "404", description = "Restaurante ainda não configurado")
    })
    public RestauranteResponse buscar() {
        return service.get();
    }

    @PutMapping("/api/restaurante")
    @PreAuthorize("hasAuthority('RESTAURANTE_EDITAR')")
    @Operation(summary = "Atualiza o restaurante configurado",
            description = "Atualiza o registro existente, sem criar outro. O id do payload "
                    + "precisa bater com o do registro configurado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante atualizado"),
        @ApiResponse(responseCode = "400", description = "Payload inválido"),
        @ApiResponse(responseCode = "404", description = "Restaurante ainda não configurado"),
        @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado"),
        @ApiResponse(responseCode = "422", description = "Id diferente do registro configurado")
    })
    public RestauranteResponse atualizar(@Valid @RequestBody RestauranteRequest request) {
        return service.update(request);
    }

    @PatchMapping("/api/restaurantes/{id}/cnpj")
    @Operation(summary = "Atualiza somente o CNPJ do restaurante")
    public RestauranteCnpjResponse update(@PathVariable Integer id,
            @Valid @RequestBody RestauranteCnpjUpdateRequest request) {
        return service.update(id, request.cnpj());
    }
}
