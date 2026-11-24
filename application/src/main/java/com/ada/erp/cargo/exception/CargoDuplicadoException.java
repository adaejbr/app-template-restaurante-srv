package com.ada.erp.cargo.exception;

import com.ada.erp.shared.exception.DuplicatedResourceException;

/** Ja existe cargo com o mesmo nome no setor (409). */
public class CargoDuplicadoException extends DuplicatedResourceException {

    public CargoDuplicadoException() {
        super("cargo.duplicado");
    }
}
