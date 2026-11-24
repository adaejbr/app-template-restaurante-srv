package com.app.restaurante.srv.application.exception;

import com.ada.erp.shared.exception.DuplicatedResourceException;

/** CPF já vinculado a outro perfil (409). */
public class CpfDuplicadoException extends DuplicatedResourceException {

    public CpfDuplicadoException() {
        super("cpf.duplicado");
    }
}
