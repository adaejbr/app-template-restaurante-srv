package com.app.controller;

import com.app.dto.LoginResponse;
import com.app.entity.Funcionario;
import com.app.mapper.FuncionarioMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final FuncionarioMapper funcionarioMapper;

    public AuthenticationController(FuncionarioMapper funcionarioMapper) {
        this.funcionarioMapper = funcionarioMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Simulação de autenticação - em produção validaria credenciais
        Funcionario funcionario = Funcionario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@restaurante.com")
                .cargo("GERENTE")
                .ativo(true)
                .build();

        LoginResponse response = funcionarioMapper.toLoginResponse(funcionario);
        response.setToken("jwt-token-simulado");
        response.setTipo("Bearer");

        return ResponseEntity.ok(response);
    }

    public record LoginRequest(String email, String senha) {}
}