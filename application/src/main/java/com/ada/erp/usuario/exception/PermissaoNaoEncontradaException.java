package com.ada.erp.usuario.exception;

import com.ada.erp.shared.exception.ResourceNotFoundException;

/** Permissao pedida nao existe (404). */
public class PermissaoNaoEncontradaException extends ResourceNotFoundException {

    public PermissaoNaoEncontradaException() {
        super("permissao.nao.encontrada");
    }
}
