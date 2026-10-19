package com.app.template._1.restaurante.srv.application.usuario;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.Authentication;

@Schema(description = "Identificação do usuário autenticado, sem credenciais.")
public record UsuarioResponse(
        @Schema(description = "Nome de usuário", example = "user") String username,
        @Schema(description = "Permissões atribuídas ao usuário", example = "[\"ROLE_USER\"]")
        List<String> authorities) {

    public static UsuarioResponse from(Authentication authentication) {
        return new UsuarioResponse(authentication.getName(),
                authentication.getAuthorities().stream().map(authority -> authority.getAuthority()).toList());
    }
}
