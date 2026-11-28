package com.ada.erp.cargo.exception;

import com.ada.erp.shared.exception.ResourceNotFoundException;

/** Setor informado nao existe (404). */
public class SetorNaoEncontradoException extends ResourceNotFoundException {

    public SetorNaoEncontradoException() {
        super("setor.nao.encontrado");
    }
}
