package com.ada.erp.restaurante.exception;

import com.ada.erp.shared.exception.DuplicatedResourceException;

/** Ja existe outro restaurante com o mesmo CNPJ (409). */
public class CnpjDuplicadoException extends DuplicatedResourceException {

    public CnpjDuplicadoException() {
        super("cnpj.duplicado");
    }
}
