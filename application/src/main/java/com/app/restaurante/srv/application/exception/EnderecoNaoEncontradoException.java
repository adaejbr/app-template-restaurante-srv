package com.app.restaurante.srv.application.exception;

import com.ada.erp.shared.exception.ResourceNotFoundException;

public class EnderecoNaoEncontradoException extends ResourceNotFoundException {

    public EnderecoNaoEncontradoException() {
        super("endereco.nao.encontrado");
    }
}
