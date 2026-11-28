package com.ada.erp.shared.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CnpjValidatorTest {
    @ParameterizedTest
    @ValueSource(strings = {"11222333000181", "11.222.333/0001-81", "04252011000110",
            "04.252.011/0001-10", "00000000000191", "12.ABC.345/01DE-35", "12abc34501de35",
            "  11.222.333/0001-81  "})
    void acceptsValidCheckDigitsAndNormalizesWithoutLosingLeadingZeroes(String value) {
        assertTrue(CnpjSupport.isValid(value));
        assertEquals(14, CnpjSupport.normalize(value).length());
        assertTrue(CnpjSupport.isValid(CnpjSupport.normalize(value)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "00000000000000", "11111111111111", "22222222222222",
            "33333333333333", "44444444444444", "55555555555555", "66666666666666",
            "77777777777777", "88888888888888", "99999999999999", "11222333000180",
            "11222333000171", "1122233300018", "112223330001811", "11-222-333/0001-81",
            "11.222333/0001-81", "11.222.333/0001-81!", "11 222 333 0001 81",
            "12.ABC.345/01DE-34", "12ABC34501DE3A", "１２２２２３３３０００１８１", "12ÁBC34501DE35"})
    void rejectsInvalidValues(String value) {
        assertFalse(CnpjSupport.isValid(value));
    }

    @Test
    void annotationUsesBeanValidationAndNotBlankEnforcesRequiredInput() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new Input("12.ABC.345/01DE-35")).isEmpty());
            assertFalse(validator.validate(new Input("11222333000180")).isEmpty());
            assertFalse(validator.validate(new Input(null)).isEmpty());
            assertFalse(validator.validate(new Input("")).isEmpty());
        }
        assertEquals("12ABC34501DE35", CnpjSupport.normalize("12.abc.345/01de-35"));
        assertEquals("04252011000110", CnpjSupport.normalize("04.252.011/0001-10"));
        assertEquals("12ßC34501DE33", CnpjSupport.normalize("12ßC34501DE33"));
    }

    private record Input(@NotBlank @CNPJ String cnpj) {
    }
}
