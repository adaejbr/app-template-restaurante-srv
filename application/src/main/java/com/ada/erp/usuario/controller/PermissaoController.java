package com.ada.erp.usuario.controller;

import java.util.List;
import com.app.restaurante.srv.application.core.filter.ApiResponse;
import com.ada.erp.usuario.dto.PermissaoCreateRequest;
import com.ada.erp.usuario.dto.PermissaoResponse;
import com.ada.erp.usuario.dto.PermissaoUpdateRequest;
import com.ada.erp.usuario.service.PermissaoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints do CRUD de permissao, cada um exigindo a chave correspondente. */
@RestController
@RequestMapping("/api/permissoes")
public class PermissaoController {

    private final PermissaoService service;

    public PermissaoController(PermissaoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSOES_VISUALIZAR')")
    public ApiResponse<List<PermissaoResponse>> listar(
            @RequestParam(name = "q", required = false) String q) {
        return ApiResponse.success(service.listar(q), "/api/permissoes");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSOES_VISUALIZAR')")
    public ApiResponse<PermissaoResponse> buscar(@PathVariable Integer id) {
        return ApiResponse.success(service.buscar(id), "/api/permissoes/" + id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERMISSOES_CRIAR')")
    public ApiResponse<PermissaoResponse> criar(@Valid @RequestBody PermissaoCreateRequest request) {
        return ApiResponse.success(service.criar(request), "/api/permissoes");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSOES_EDITAR')")
    public ApiResponse<PermissaoResponse> atualizar(@PathVariable Integer id,
            @Valid @RequestBody PermissaoUpdateRequest request) {
        return ApiResponse.success(service.atualizar(id, request), "/api/permissoes/" + id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERMISSOES_EXCLUIR')")
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }
}
