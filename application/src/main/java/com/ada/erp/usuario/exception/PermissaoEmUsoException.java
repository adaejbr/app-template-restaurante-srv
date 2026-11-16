package com.ada.erp.usuario.exception;

import com.ada.erp.shared.exception.DuplicatedResourceException;

/** A permissao ainda esta vinculada a algum cargo (409). */
public class PermissaoEmUsoException extends DuplicatedResourceException {

    public PermissaoEmUsoException() {
        super("permissao.em.uso");
    }
}
