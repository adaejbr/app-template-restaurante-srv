package com.ada.erp.restaurante.exception;

import com.ada.erp.shared.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;

/**
 * CNPJ fora do formato aceito (400).
 *
 * <p>Entra na hierarquia de regra de negocio, mas redefine o status: e erro de
 * validacao do payload, nao violacao de regra, e o contrato da API ja e 400.
 */
public class CnpjInvalidoException extends BusinessRuleException {

    public CnpjInvalidoException() {
        super(HttpStatus.BAD_REQUEST, "cnpj.invalido");
    }
}
