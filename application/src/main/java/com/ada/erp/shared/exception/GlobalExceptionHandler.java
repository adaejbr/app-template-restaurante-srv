package com.ada.erp.shared.exception;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Tratamento unico de erros da API (INFRA-03, #56).
 *
 * <p>Toda resposta de erro sai daqui no mesmo formato: {@code {"mensagem": "..."}}.
 * O texto vem do bundle {@code messages.properties}, resolvido no idioma da
 * requisicao, com o {@code getMessage()} da excecao so como ultimo recurso.
 *
 * <p>As excecoes de dominio tem handler proprio; qualquer outra cai no
 * generico, que preserva o {@code @ResponseStatus} da classe para nao
 * rebaixar um erro legado para 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(ResourceNotFoundException exception) {
        return responder(HttpStatus.NOT_FOUND, exception.getChave(), exception.getArgs());
    }

    /**
     * Regras de negocio: 422 por padrao, 409 para {@link DuplicatedResourceException}
     * e 400 para a excecao que redefine o status, como a de CNPJ invalido.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> regraDeNegocio(BusinessRuleException exception) {
        return responder(exception.getStatus(), exception.getChave(), exception.getArgs());
    }

    @ExceptionHandler(AccessDeniedBusinessException.class)
    public ResponseEntity<Map<String, String>> acessoNegadoDeDominio(
            AccessDeniedBusinessException exception) {
        return responder(HttpStatus.FORBIDDEN, exception.getChave(), exception.getArgs());
    }

    /** O Spring Security nega o acesso antes de chegar aqui, mas cobre chamadas internas. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(AccessDeniedException exception) {
        return responder(HttpStatus.FORBIDDEN, "acesso.negado");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> corpoInvalido(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getAllErrors().stream()
                .map(this::descrever)
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(ConstraintViolationException exception) {
        String mensagem = exception.getConstraintViolations().stream()
                .map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> payloadIlegivel(HttpMessageNotReadableException exception) {
        return responder(HttpStatus.BAD_REQUEST, "payload.invalido");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> rotaInexistente(NoResourceFoundException exception) {
        return responder(HttpStatus.NOT_FOUND, "recurso.nao.encontrado");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> metodoNaoSuportado(
            HttpRequestMethodNotSupportedException exception) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "metodo.nao.suportado");
    }

    /**
     * Exceptions do proprio Spring MVC, como parametro ausente ou tipo nao
     * suportado. O status definido pelo framework e preservado, em vez de
     * cair no 500 do handler generico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> naoTratada(Exception exception) {
        if (exception instanceof ErrorResponse erro) {
            ProblemDetail corpo = erro.getBody();
            String mensagem = corpo.getDetail() != null ? corpo.getDetail() : corpo.getTitle();
            return responderComMensagem(erro.getStatusCode(), mensagem);
        }
        ResponseStatus anotada = AnnotatedElementUtils
                .findMergedAnnotation(exception.getClass(), ResponseStatus.class);
        if (anotada != null) {
            String mensagem = anotada.reason().isEmpty() ? exception.getMessage() : anotada.reason();
            return responderComMensagem(anotada.code(), mensagem);
        }
        log.error("Erro nao tratado: {}", exception.getMessage(), exception);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "erro.interno");
    }

    /** Monta a resposta de erro; a mensagem ja chega resolvida pelo bundle. */
    private ResponseEntity<Map<String, String>> responder(HttpStatus status, String chave, Object... args) {
        return responderComMensagem(status, resolver(chave, args));
    }

    private ResponseEntity<Map<String, String>> responderComMensagem(HttpStatusCode status, String mensagem) {
        return ResponseEntity.status(status)
                .body(Map.of("mensagem", mensagem == null ? motivoPadrao(status) : mensagem));
    }

    private String motivoPadrao(HttpStatusCode status) {
        HttpStatus conhecido = HttpStatus.resolve(status.value());
        return conhecido == null ? "Erro" : conhecido.getReasonPhrase();
    }

    private String resolver(String chave, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(chave, args, locale);
        } catch (NoSuchMessageException excecao) {
            log.warn("Chave de mensagem ausente no bundle: {}", chave);
            return chave;
        }
    }

    private String descrever(ObjectError erro) {
        if (erro instanceof FieldError campo) {
            return campo.getField() + ": " + Objects.toString(erro.getDefaultMessage(), "");
        }
        return Objects.toString(erro.getDefaultMessage(), "");
    }
}
