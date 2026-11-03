package com.ada.erp.restaurante.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CnpjDuplicadoException extends RuntimeException {
    public CnpjDuplicadoException() {
        super("CNPJ já cadastrado para outro restaurante");
    }
}
