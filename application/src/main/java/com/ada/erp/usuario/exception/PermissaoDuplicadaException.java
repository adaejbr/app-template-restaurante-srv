package com.ada.erp.usuario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PermissaoDuplicadaException extends RuntimeException {

    public PermissaoDuplicadaException() {
        super("Chave de permissão já cadastrada");
    }
}
