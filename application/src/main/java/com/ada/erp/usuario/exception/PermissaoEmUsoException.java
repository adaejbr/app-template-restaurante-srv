package com.ada.erp.usuario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PermissaoEmUsoException extends RuntimeException {

    public PermissaoEmUsoException() {
        super("Permissão vinculada a um cargo não pode ser removida");
    }
}
