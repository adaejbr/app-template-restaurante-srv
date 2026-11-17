package com.ada.erp.cargo.controller;

import java.util.List;
import com.ada.erp.cargo.dto.CargoCreateRequest;
import com.ada.erp.cargo.dto.CargoResponse;
import com.ada.erp.cargo.dto.CargoUpdateRequest;
import com.ada.erp.cargo.service.CargoFuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints do CRUD de cargo (CARGO-02, #66), protegidos pelas chaves CARGOS_*. */
@RestController
@RequestMapping("/api/cargos")
public class CargoFuncionarioController {

    private final CargoFuncionarioService service;

    public CargoFuncionarioController(CargoFuncionarioService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CARGOS_VISUALIZAR')")
    public List<CargoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CARGOS_VISUALIZAR')")
    public CargoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CARGOS_CRIAR')")
    public CargoResponse criar(@Valid @RequestBody CargoCreateRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CARGOS_EDITAR')")
    public CargoResponse atualizar(@PathVariable Integer id,
            @Valid @RequestBody CargoUpdateRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CARGOS_EXCLUIR')")
    public void desativar(@PathVariable Integer id) {
        service.desativar(id);
    }
}
