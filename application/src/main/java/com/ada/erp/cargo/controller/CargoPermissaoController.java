package com.ada.erp.cargo.controller;

import java.util.List;
import java.util.Map;
import com.ada.erp.cargo.dto.CargoPermissaoUpdateRequest;
import com.ada.erp.cargo.exception.PermissoesInexistentesException;
import com.ada.erp.cargo.service.CargoPermissaoService;
import com.ada.erp.usuario.dto.PermissaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Consulta e substituicao das permissoes de um cargo (CARGO-03, #57). */
@RestController
@RequestMapping("/api/cargos/{idCargo}/permissoes")
public class CargoPermissaoController {

    private final CargoPermissaoService service;

    public CargoPermissaoController(CargoPermissaoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CARGOS_VISUALIZAR')")
    public List<PermissaoResponse> buscarPermissoes(@PathVariable Integer idCargo) {
        return service.buscarPermissoes(idCargo);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CARGOS_EDITAR')")
    public void atualizarPermissoes(@PathVariable Integer idCargo,
            @Valid @RequestBody CargoPermissaoUpdateRequest request) {
        service.atualizarPermissoes(idCargo, request.permissoes());
    }

    /** Mantem a mensagem no corpo ate a INFRA-03 centralizar os erros. */
    @ExceptionHandler(PermissoesInexistentesException.class)
    public ResponseEntity<Map<String, String>> permissoesInexistentes(
            PermissoesInexistentesException exception) {
        return ResponseEntity.unprocessableEntity()
                .body(Map.of("mensagem", exception.getMessage()));
    }
}
