package com.ada.erp.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Forca minima de senha, alinhada ao OWASP ASVS (V2.1): comprimento minimo,
 * espacos permitidos, sem regras de composicao obrigatorias e bloqueio de
 * senhas comuns. Combine com NotNull quando o campo for obrigatorio.
 *
 * <p>O limite em bytes existe porque o BCrypt so considera os primeiros 72
 * bytes e o encoder do Spring Security rejeita entradas maiores. Ele deixa de
 * ser necessario se o projeto migrar para Argon2id.
 */
@Documented
@Constraint(validatedBy = SenhaForteValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE,
        ElementType.TYPE_USE, ElementType.RECORD_COMPONENT, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {

    String message() default "{com.ada.erp.shared.validation.SenhaForte.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Minimo de caracteres (code points). */
    int min() default 8;

    /** Maximo de caracteres (code points). */
    int max() default 64;

    /** Maximo de bytes em UTF-8, limite do BCrypt. */
    int maxBytes() default 72;
}
