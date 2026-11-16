package com.ada.erp.usuario.controller;

import java.util.List;
import java.util.Map;
import com.ada.erp.usuario.dto.PermissaoCreateRequest;
import com.ada.erp.usuario.dto.PermissaoResponse;
import com.ada.erp.usuario.dto.PermissaoUpdateRequest;
import com.ada.erp.usuario.exception.ChaveImutavelException;
import com.ada.erp.usuario.service.PermissaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    public List<PermissaoResponse> listar(@RequestParam(name = "q", required = false) String q) {
        return service.listar(q);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSOES_VISUALIZAR')")
    public PermissaoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERMISSOES_CRIAR')")
    public PermissaoResponse criar(@Valid @RequestBody PermissaoCreateRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSOES_EDITAR')")
    public PermissaoResponse atualizar(@PathVariable Integer id,
            @Valid @RequestBody PermissaoUpdateRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERMISSOES_EXCLUIR')")
    public void excluir(@PathVariable Integer id) {
        service.excluir(id);
    }

    /** Mantem a mensagem de imutabilidade no corpo ate a INFRA-03 centralizar os erros. */
    @ExceptionHandler(ChaveImutavelException.class)
    public ResponseEntity<Map<String, String>> chaveImutavel(ChaveImutavelException exception) {
        return ResponseEntity.unprocessableEntity().body(Map.of("mensagem", exception.getMessage()));
    }
}
