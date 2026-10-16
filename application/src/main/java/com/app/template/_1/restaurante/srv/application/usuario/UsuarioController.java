package com.app.template._1.restaurante.srv.application.usuario;

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
    @Operation(summary = "Consultar usuário autenticado", responses = {
        @ApiResponse(responseCode = "200", description = "Identidade da sessão autenticada",
                content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(value =
                                "{\"username\":\"user\",\"authorities\":[\"ROLE_USER\"]}"))),
        @ApiResponse(responseCode = "401", description = "Sessão ausente ou não autenticada",
                content = @Content)
    })
    public UsuarioResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return UsuarioResponse.from(authentication);
    }
}
