package com.ada.erp.cargo.exception;

import com.ada.erp.shared.exception.ResourceNotFoundException;

/** Cargo pedido nao existe (404). */
public class CargoNaoEncontradoException extends ResourceNotFoundException {

    public CargoNaoEncontradoException() {
        super("cargo.nao.encontrado");
    }
}
