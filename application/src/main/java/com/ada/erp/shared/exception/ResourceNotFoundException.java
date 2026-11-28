package com.ada.erp.shared.exception;

import java.util.Arrays;

/**
 * Recurso pedido nao existe: 404 Not Found.
 *
 * <p>Fica fora da {@link BusinessRuleException} porque nao e regra de negocio,
 * mas segue o mesmo contrato de chave do bundle + argumentos.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String chave;
    private final Object[] args;

    protected ResourceNotFoundException(String chave, Object... args) {
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
