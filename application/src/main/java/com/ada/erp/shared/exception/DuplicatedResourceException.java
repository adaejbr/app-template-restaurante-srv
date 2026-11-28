package com.ada.erp.shared.exception;

import org.springframework.http.HttpStatus;

/** Recurso ja cadastrado: 409 Conflict. */
public class DuplicatedResourceException extends BusinessRuleException {

    protected DuplicatedResourceException(String chave, Object... args) {
        super(HttpStatus.CONFLICT, chave, args);
    }
}
