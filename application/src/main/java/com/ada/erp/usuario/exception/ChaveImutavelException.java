package com.ada.erp.usuario.exception;

import com.ada.erp.shared.exception.BusinessRuleException;

/** A chave da permissao nao pode ser alterada depois de criada (422). */
public class ChaveImutavelException extends BusinessRuleException {

    public ChaveImutavelException() {
        super("chave.imutavel");
    }
}
