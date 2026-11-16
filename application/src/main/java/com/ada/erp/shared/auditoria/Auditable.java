package com.ada.erp.shared.auditoria;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marca metodos de escrita cujo sucesso deve gerar registro de auditoria. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Nome da acao registrada; vazio usa a assinatura do metodo. */
    String acao() default "";
}
