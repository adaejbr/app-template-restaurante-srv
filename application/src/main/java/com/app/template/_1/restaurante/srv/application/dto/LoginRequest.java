package com.app.template._1.restaurante.srv.application.dto;

/**
 * DTO de entrada para autenticação (task j-05).
 *
 * @param email e-mail do funcionário (corresponde ao campo `login` da tabela funcionario)
 * @param senha senha em texto plano
 */
public record LoginRequest(String email, String senha) {
}
