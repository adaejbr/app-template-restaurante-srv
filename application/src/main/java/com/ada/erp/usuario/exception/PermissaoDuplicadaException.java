package com.ada.erp.usuario.exception;

import com.ada.erp.shared.exception.DuplicatedResourceException;

/** Ja existe permissao com a mesma chave (409). */
public class PermissaoDuplicadaException extends DuplicatedResourceException {

    public PermissaoDuplicadaException() {
        super("permissao.duplicada");
    }
}
