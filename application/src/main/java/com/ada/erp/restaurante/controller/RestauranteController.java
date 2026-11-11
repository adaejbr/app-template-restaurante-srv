package com.ada.erp.restaurante.controller;

import com.ada.erp.restaurante.dto.RestauranteCnpjResponse;
import com.ada.erp.restaurante.dto.RestauranteCnpjUpdateRequest;
import com.ada.erp.restaurante.service.RestauranteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Minimal CNPJ update endpoint until the full restaurant registration API is delivered. */
@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteController {
    private final RestauranteService service;

    public RestauranteController(RestauranteService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/cnpj")
    public RestauranteCnpjResponse update(@PathVariable Integer id,
            @Valid @RequestBody RestauranteCnpjUpdateRequest request) {
        return service.update(id, request.cnpj());
    }
}
