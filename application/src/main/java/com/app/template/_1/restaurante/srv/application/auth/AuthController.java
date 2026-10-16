package com.app.template._1.restaurante.srv.application.auth;

import com.app.template._1.restaurante.srv.application.usuario.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contexts;
    private final SessionAuthenticationStrategy sessions;

    public AuthController(AuthenticationConfiguration configuration, SecurityContextRepository contexts,
            SessionAuthenticationStrategy sessions) throws Exception {
        this.authenticationManager = configuration.getAuthenticationManager();
        this.contexts = contexts;
        this.sessions = sessions;
    }

    @Operation(summary = "Obter token CSRF",
            description = "Copie token para o cabeçalho X-CSRF-TOKEN do login. "
                    + "Mantenha o cookie da sessão e obtenha um novo token após login ou logout.")
    @SecurityRequirements
    @GetMapping("/api/auth/csrf")
    public CsrfResponse csrf(@Parameter(hidden = true) CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @Operation(summary = "Autenticar usuário",
            description = "Valida credenciais e cria uma sessão por cookie JSESSIONID. "
                    + "Antes de executar, obtenha um token em GET /api/auth/csrf.",
            parameters = @Parameter(name = "X-CSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                    description = "Valor de token retornado por GET /api/auth/csrf", schema = @Schema(type = "string")))
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado; sessão criada",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = UsuarioResponse.class),
                        examples = @ExampleObject(value = "{\"username\":\"user\",\"authorities\":[\"ROLE_USER\"]}"))),
        @ApiResponse(responseCode = "400", description = "JSON inválido ou campos obrigatórios ausentes",
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content),
        @ApiResponse(responseCode = "403", description = "Token CSRF ausente ou inválido", content = @Content)
    })
    @PostMapping("/api/auth/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest credentials,
            HttpServletRequest request, HttpServletResponse response) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(credentials.username(), credentials.password()));
        sessions.onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return UsuarioResponse.from(authentication);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    void invalidCredentials() {
    }

    public record CsrfResponse(
            @Schema(example = "X-CSRF-TOKEN") String headerName,
            @Schema(description = "Token temporário vinculado à sessão", example = "copie-o-token-da-resposta")
            String token) {
    }
}
