package com.ada.erp.restaurante.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CnpjInvalidoException extends RuntimeException {
    public CnpjInvalidoException() {
        super("CNPJ inválido");
    }
}
