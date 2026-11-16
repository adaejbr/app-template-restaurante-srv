package com.ada.erp.usuario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ChaveImutavelException extends RuntimeException {

    public ChaveImutavelException() {
        super("A chave da permissão é imutável após a criação");
    }
}
