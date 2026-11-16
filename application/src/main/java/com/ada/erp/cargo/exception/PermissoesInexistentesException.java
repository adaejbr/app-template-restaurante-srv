package com.ada.erp.cargo.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** O payload aponta permissao que nao existe; nenhum vinculo e alterado. */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PermissoesInexistentesException extends RuntimeException {

    public PermissoesInexistentesException(List<Integer> idsPermissoes) {
        super("Permissões inexistentes: " + idsPermissoes);
    }
}
