package com.ada.erp.shared.exception;

import java.util.Arrays;
import org.springframework.http.HttpStatus;

/**
 * Base das excecoes de regra de negocio (INFRA-03, #56).
 *
 * <p>A mensagem nao mora no codigo: a excecao guarda a chave do bundle
 * {@code messages.properties} e os argumentos, e o {@link GlobalExceptionHandler}
 * resolve o texto no idioma da requisicao. {@link #getMessage()} devolve a chave
 * com os argumentos, e serve so para log, nunca para a resposta.
 */
public class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;
    private final String chave;
    private final Object[] args;

    protected BusinessRuleException(HttpStatus status, String chave, Object... args) {
        super(paraLog(chave, args));
        this.status = status;
        this.chave = chave;
        this.args = args.clone();
    }

    /** Regra de negocio sem status proprio: 422, o padrao da hierarquia. */
    protected BusinessRuleException(String chave, Object... args) {
        this(HttpStatus.UNPROCESSABLE_ENTITY, chave, args);
    }

    /** Status HTTP que a excecao pede. */
    public HttpStatus getStatus() {
        return status;
    }

    /** Chave do bundle de mensagens. */
    public String getChave() {
        return chave;
    }

    /** Argumentos do template da mensagem. */
    public Object[] getArgs() {
        return args.clone();
    }

    private static String paraLog(String chave, Object[] args) {
        return args.length == 0 ? chave : chave + " " + Arrays.toString(args);
    }
}
