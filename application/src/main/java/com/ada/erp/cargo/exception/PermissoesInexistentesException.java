package com.ada.erp.cargo.exception;

import java.util.List;
import com.ada.erp.shared.exception.BusinessRuleException;

/** O payload aponta permissao que nao existe; nenhum vinculo e alterado (422). */
public class PermissoesInexistentesException extends BusinessRuleException {

    public PermissoesInexistentesException(List<Integer> idsPermissoes) {
        // Vira String: o MessageFormat formataria 9999 como 9.999 em pt-BR.
        super("permissoes.inexistentes", String.valueOf(idsPermissoes));
    }
}
