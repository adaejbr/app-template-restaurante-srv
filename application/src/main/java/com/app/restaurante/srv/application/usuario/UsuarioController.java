package com.app.restaurante.srv.application.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {
    @GetMapping("/api/usuarios/me")
    @Operation(summary = "Consultar usuário autenticado pelo JWT", responses = {
        @ApiResponse(responseCode = "200", description = "Identidade do funcionário autenticado",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(value =
                                "{\"username\":\"user\",\"authorities\":[\"ROLE_USER\"]}"))),
        @ApiResponse(responseCode = "401", description = "Bearer token ausente ou inválido", content = @Content)
    })
    public UsuarioResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return UsuarioResponse.from(authentication);
    }
}
