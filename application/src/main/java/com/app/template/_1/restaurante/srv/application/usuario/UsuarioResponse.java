package com.app.template._1.restaurante.srv.application.usuario;

// O DTO define explicitamente os campos expostos pela API.
public record UsuarioResponse(Long id, String nome, String email) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
