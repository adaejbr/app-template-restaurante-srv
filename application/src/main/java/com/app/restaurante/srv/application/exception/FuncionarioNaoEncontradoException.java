package com.app.restaurante.srv.application.exception;

import com.ada.erp.shared.exception.ResourceNotFoundException;

/** Funcionário pedido não existe (404). */
public class FuncionarioNaoEncontradoException extends ResourceNotFoundException {

    public FuncionarioNaoEncontradoException() {
        super("funcionario.nao.encontrado");
    }
}
