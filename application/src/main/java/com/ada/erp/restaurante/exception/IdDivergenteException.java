package com.ada.erp.restaurante.exception;

import com.ada.erp.shared.exception.BusinessRuleException;

/** O id do payload nao e o do restaurante configurado (422). */
public class IdDivergenteException extends BusinessRuleException {

    public IdDivergenteException() {
        super("id.divergente");
    }
}
