package com.ada.erp.usuario.exception;

import com.ada.erp.shared.exception.AccessDeniedBusinessException;

/**
 * O endpoint administrativo de senha nao altera a senha de quem o chama (403):
 * a troca da propria senha exige a senha atual e tem endpoint proprio (RF002).
 * Sem esta regra, um token de gerente roubado tomaria a conta sem a senha atual.
 */
public class AutoAlteracaoSenhaNegadaException extends AccessDeniedBusinessException {

    public AutoAlteracaoSenhaNegadaException() {
        super("senha.auto.alteracao.negada");
    }
}
