package com.ada.erp.shared.exception;

import java.util.Arrays;

/** A autenticacao existe, mas a permissao nao cobre a acao: 403 Forbidden. */
public class AccessDeniedBusinessException extends RuntimeException {

    private final String chave;
    private final Object[] args;

    protected AccessDeniedBusinessException(String chave, Object... args) {
        super(args.length == 0 ? chave : chave + " " + Arrays.toString(args));
        this.chave = chave;
        this.args = args.clone();
    }

    public String getChave() {
        return chave;
    }

    public Object[] getArgs() {
        return args.clone();
    }
}
