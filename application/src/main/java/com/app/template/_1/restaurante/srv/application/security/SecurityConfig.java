package com.app.template._1.restaurante.srv.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuração de segurança mínima para a task j-05 (login).
 *
 * NOTA: esta é uma implementação mínima criada pela j-05 para viabilizar o login.
 * A task j-04 (Spring Security + JWT) deve revisar/substituir este componente
 * (cadeia de filtros, JwtAuthenticationFilter, permissões de endpoints etc.).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/** Libera o endpoint de login e Swagger; todo o resto exige autenticação. */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/login", "/swagger-ui/**", "/swagger-ui.html",
								"/v3/api-docs/**")
						.permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					response.setCharacterEncoding("UTF-8");
					response.setContentType("application/json");
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("{\"mensagem\":\"Não autenticado\"}");
				}));
		return http.build();
	}

	/** AuthenticationManager exposto como bean (exigência da task j-05). */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	/**
	 * TODO j-11 (bcrypt): substituir por BCryptPasswordEncoder.
	 * Por ora aceita senha em texto plano (V2__seed_data.sql).
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new PasswordEncoder() {
			@Override
			public String encode(CharSequence rawPassword) {
				return rawPassword.toString();
			}

			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				return rawPassword.toString().equals(encodedPassword);
			}
		};
	}
}
