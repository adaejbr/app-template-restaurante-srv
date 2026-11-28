package com.ada.erp.shared.validation;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida {@link SenhaForte}. Cada regra violada gera a sua propria mensagem,
 * para o cliente saber o que corrigir; o valor da senha nunca entra na mensagem.
 */
public class SenhaForteValidator implements ConstraintValidator<SenhaForte, String> {

    /**
     * Amostra de senhas mais usadas/vazadas. Lista minima: a verificacao contra
     * uma base completa de senhas vazadas fica para uma task propria.
     */
    private static final Set<String> SENHAS_COMUNS = Set.of(
            "12345678", "123456789", "1234567890", "87654321", "11111111", "00000000",
            "password", "password1", "password123", "senha123", "senha1234", "12345678910",
            "qwertyui", "qwerty123", "abc12345", "abcd1234", "iloveyou", "admin123",
            "admin1234", "administrador", "mudar123", "trocar123", "restaurante", "brasil123");

    private int min;
    private int max;
    private int maxBytes;

    @Override
    public void initialize(SenhaForte anotacao) {
        this.min = anotacao.min();
        this.max = anotacao.max();
        this.maxBytes = anotacao.maxBytes();
    }

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        if (senha == null) {
            return true;
        }
        int caracteres = senha.codePointCount(0, senha.length());
        if (senha.isBlank()) {
            return violar(context, "{com.ada.erp.shared.validation.SenhaForte.vazia}");
        }
        if (caracteres < min) {
            return violar(context, "{com.ada.erp.shared.validation.SenhaForte.curta}");
        }
        if (caracteres > max || senha.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            return violar(context, "{com.ada.erp.shared.validation.SenhaForte.longa}");
        }
        if (SENHAS_COMUNS.contains(senha.toLowerCase(Locale.ROOT)) || caracteresRepetidos(senha)) {
            return violar(context, "{com.ada.erp.shared.validation.SenhaForte.comum}");
        }
        return true;
    }

    /** Senha formada por um unico caractere repetido, como "aaaaaaaa". */
    private boolean caracteresRepetidos(String senha) {
        int primeiro = senha.codePointAt(0);
        return senha.codePoints().allMatch(codePoint -> codePoint == primeiro);
    }

    private boolean violar(ConstraintValidatorContext context, String template) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(template).addConstraintViolation();
        return false;
    }
}
