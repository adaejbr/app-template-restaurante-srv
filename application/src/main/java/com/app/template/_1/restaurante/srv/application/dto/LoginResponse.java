package com.app.template._1.restaurante.srv.application.dto;

/**
 * DTO de saída do login (task j-05).
 *
 * @param token  token JWT gerado
 * @param userId id do funcionário autenticado
 * @param nome   nome completo (via Perfil)
 * @param cargo  cargo ativo do funcionário
 */
public record LoginResponse(String token, Integer userId, String nome, String cargo) {
}
