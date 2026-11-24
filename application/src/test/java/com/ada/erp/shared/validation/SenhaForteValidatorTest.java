package com.ada.erp.shared.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Regras do @SenhaForte (FUNC-05, #67), alinhadas ao OWASP ASVS V2.1.
class SenhaForteValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    record Payload(@SenhaForte String senha) {
    }

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Cozinha-Temporaria-2026",
        "frase longa com espacos",
        "senhasemregrasdecomposicao",
        "çãõéíóúçãõé",
        "12345678a"
    })
    void aceitaSenhasFortes(String senha) {
        assertThat(validar(senha)).isEmpty();
    }

    @Test
    void nuloFicaParaONotNull() {
        assertThat(validar(null)).isEmpty();
    }

    @Test
    void aceitaExatamenteOsLimites() {
        assertThat(validar("a1b2c3d4")).isEmpty();
        assertThat(validar("a".repeat(63) + "b")).isEmpty();
    }

    @Test
    void rejeitaSenhaCurta() {
        assertThat(mensagens("a1b2c3d")).containsExactly("A senha deve ter no mínimo 8 caracteres");
    }

    @Test
    void rejeitaSenhaComMaisDe64Caracteres() {
        assertThat(mensagens("a".repeat(64) + "b"))
                .containsExactly("A senha deve ter no máximo 64 caracteres e 72 bytes");
    }

    @Test
    void rejeitaSenhaAcimaDe72BytesMesmoComPoucosCaracteres() {
        // "çã" ocupa 4 bytes: 36 caracteres = 72 bytes (limite), 37 caracteres = 74 bytes.
        assertThat(validar("çã".repeat(18))).isEmpty();
        assertThat(mensagens("çã".repeat(18) + "é"))
                .containsExactly("A senha deve ter no máximo 64 caracteres e 72 bytes");
    }

    @Test
    void rejeitaSenhaSoComEspacos() {
        assertThat(mensagens("          "))
                .containsExactly("A senha não pode ser composta apenas por espaços");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "PASSWORD123", "Senha123", "admin123", "aaaaaaaaaa"})
    void rejeitaSenhasComunsOuPrevisiveis(String senha) {
        assertThat(mensagens(senha)).containsExactly("Senha muito comum ou previsível; escolha outra");
    }

    private Set<ConstraintViolation<Payload>> validar(String senha) {
        return validator.validate(new Payload(senha));
    }

    private Set<String> mensagens(String senha) {
        return Set.copyOf(validar(senha).stream().map(ConstraintViolation::getMessage).toList());
    }
}
