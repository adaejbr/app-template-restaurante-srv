package com.ada.erp.shared.auditoria;

import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registro minimo de auditoria para metodos anotados com {@link Auditable}.
 * Loga somente apos sucesso; o modelo persistido fica com a AUDIT-03.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger LOG = LoggerFactory.getLogger(AuditAspect.class);

    @Around("@annotation(auditable)")
    public Object registrar(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object resultado = joinPoint.proceed();
        String acao = auditable.acao().isBlank()
                ? joinPoint.getSignature().toShortString()
                : auditable.acao();
        LOG.info("auditoria acao={} entidadeId={}", acao, extrairId(resultado));
        return resultado;
    }

    private Object extrairId(Object resultado) {
        if (resultado == null) {
            return null;
        }
        for (String nome : List.of("getId", "id")) {
            try {
                Method metodo = resultado.getClass().getMethod(nome);
                return metodo.invoke(resultado);
            } catch (ReflectiveOperationException exception) {
                // Tenta o proximo nome de acesso ao id.
            }
        }
        return null;
    }
}
