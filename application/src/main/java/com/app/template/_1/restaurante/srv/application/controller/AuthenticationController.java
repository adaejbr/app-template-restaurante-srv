package com.app.template._1.restaurante.srv.application.controller;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;
import com.app.template._1.restaurante.srv.application.dto.LoginRequest;
import com.app.template._1.restaurante.srv.application.dto.LoginResponse;
import com.app.template._1.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.template._1.restaurante.srv.application.security.CustomUserDetails;
import com.app.template._1.restaurante.srv.application.security.JwtUtils;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final CargoFuncionarioRepository cargoFuncionarioRepository;
    private final JwtUtils jwtUtils;

    public AuthenticationController(AuthenticationManager authenticationManager,
            CargoFuncionarioRepository cargoFuncionarioRepository, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.cargoFuncionarioRepository = cargoFuncionarioRepository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar funcionário e emitir JWT")
    @SecurityRequirements
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = LoginResponse.class),
                        examples = @ExampleObject(value =
                                "{\"token\":\"eyJ...\",\"userId\":1,\"nome\":\"Gerente Padrao\","
                                        + "\"cargo\":\"GERENTE\"}"))),
        @ApiResponse(responseCode = "400", description = "E-mail ou senha ausente/inválido",
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou funcionário inativo",
                content = @Content)
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.senha()));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensagem", "Credenciais inválidas"));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Funcionario funcionario = userDetails.getFuncionario();
        String nome = funcionario.getPerfil().getNome();
        String cargo = cargoFuncionarioRepository
                .findFirstByIdFuncionarioAndStatus(funcionario.getId(), "ATIVO")
                .map(cargoFuncionario -> cargoFuncionario.getCargo().getNome())
                .orElse(null);

        return ResponseEntity.ok(new LoginResponse(
                jwtUtils.generateToken(userDetails), funcionario.getId(), nome, cargo));
    }
}
