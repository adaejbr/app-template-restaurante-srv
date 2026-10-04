package com.app.template._1.restaurante.srv.application.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Usuários")
public class UsuarioController {

    @Operation(summary = "Consultar usuário autenticado",
            description = "Retorna o nome e as permissões do usuário associado à sessão do login.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário autenticado",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = UsuarioResponse.class),
                        examples = @ExampleObject(value = "{\"username\":\"user\",\"authorities\":[\"ROLE_USER\"]}"))),
        @ApiResponse(responseCode = "401", description = "Sessão ausente ou expirada", content = @Content)
    })
    @GetMapping("/api/usuarios/me")
    public UsuarioResponse me(Authentication authentication) {
        return UsuarioResponse.from(authentication);
    }
}
