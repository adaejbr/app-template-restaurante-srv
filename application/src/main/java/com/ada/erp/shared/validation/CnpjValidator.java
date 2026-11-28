package com.ada.erp.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<CNPJ, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || CnpjSupport.isValid(value);
    }
}
