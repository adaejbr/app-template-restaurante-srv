package com.app.restaurante.srv.application.exception;

import com.ada.erp.shared.exception.DuplicatedResourceException;

/** Login (e-mail) já vinculado a outro funcionário (409). */
public class LoginDuplicadoException extends DuplicatedResourceException {

    public LoginDuplicadoException() {
        super("login.duplicado");
    }
}
