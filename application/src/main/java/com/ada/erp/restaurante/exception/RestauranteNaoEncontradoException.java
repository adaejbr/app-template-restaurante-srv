package com.ada.erp.restaurante.exception;

import com.ada.erp.shared.exception.ResourceNotFoundException;

/** Restaurante pedido nao existe (404). */
public class RestauranteNaoEncontradoException extends ResourceNotFoundException {

    public RestauranteNaoEncontradoException() {
        super("restaurante.nao.encontrado");
    }
}
