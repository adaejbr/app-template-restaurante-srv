package com.ada.erp.usuario.exception;

import com.ada.erp.shared.exception.ResourceNotFoundException;

/** Funcionario pedido nao existe (404). */
public class FuncionarioNaoEncontradoException extends ResourceNotFoundException {

    public FuncionarioNaoEncontradoException() {
        super("funcionario.nao.encontrado");
    }
}
