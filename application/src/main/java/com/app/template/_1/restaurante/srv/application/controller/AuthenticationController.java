package com.app.template._1.restaurante.srv.application.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;
import com.app.template._1.restaurante.srv.application.dto.LoginRequest;
import com.app.template._1.restaurante.srv.application.dto.LoginResponse;
import com.app.template._1.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.template._1.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.template._1.restaurante.srv.application.security.CustomUserDetailsService;
import com.app.template._1.restaurante.srv.application.security.JwtUtils;

/**
 * Endpoint de autenticação - POST /api/auth/login (task j-05).
 *
 * Recebe email+senha, autentica via AuthenticationManager e devolve um token JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthenticationManager authenticationManager;
	private final CustomUserDetailsService userDetailsService;
	private final FuncionarioRepository funcionarioRepository;
	private final CargoFuncionarioRepository cargoFuncionarioRepository;
	private final JwtUtils jwtUtils;

	public AuthenticationController(AuthenticationManager authenticationManager,
			CustomUserDetailsService userDetailsService,
			FuncionarioRepository funcionarioRepository,
			CargoFuncionarioRepository cargoFuncionarioRepository,
			JwtUtils jwtUtils) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.funcionarioRepository = funcionarioRepository;
		this.cargoFuncionarioRepository = cargoFuncionarioRepository;
		this.jwtUtils = jwtUtils;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		// 1. Autentica (credenciais inválidas -> 401)
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("mensagem", "Credenciais inválidas"));
		}

		// 2. Recarrega o usuário e gera o token JWT
		UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
		String token = jwtUtils.generateToken(userDetails);

		// 3. Monta a resposta (userId, nome, cargo)
		Funcionario funcionario = ((com.app.template._1.restaurante.srv.application.security.CustomUserDetails) userDetails)
				.getFuncionario();
		String nome = funcionario.getPerfil().getNome();
		String cargo = cargoFuncionarioRepository
				.findFirstByIdFuncionarioAndStatus(funcionario.getId(), "ATIVO")
				.map(cf -> cf.getCargo().getNome())
				.orElse(null);

		return ResponseEntity.ok(new LoginResponse(token, funcionario.getId(), nome, cargo));
	}
}
