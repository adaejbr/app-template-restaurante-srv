package com.ada.erp.cargo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CargoNaoEncontradoException extends RuntimeException {

    public CargoNaoEncontradoException() {
        super("Cargo não encontrado");
    }
}
