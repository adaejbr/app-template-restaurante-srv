package com.app.template._1.restaurante.srv.application.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint entryPoint = (request, response, exception) -> {
            boolean expired = false;
            // A causa vem do decoder, depois da verificacao da assinatura.
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof JwtValidationException validation) {
                    expired = validation.getErrors().stream()
                            .anyMatch(error -> "token_expired".equals(error.getErrorCode()));
                    break;
                }
            }
            response.setStatus(expired ? 403 : 401);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if (!expired) response.setHeader("WWW-Authenticate", "Bearer");
            response.getWriter().write(expired
                    ? "{\"status\":403,\"erro\":\"Token expirado\"}"
                    : "{\"status\":401,\"erro\":\"Token ausente ou invalido\"}");
        };

        return http
                // API sem cookies de autenticacao: cada chamada envia seu Bearer token.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint))
                .oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(entryPoint))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
        byte[] bytes = Base64.getDecoder().decode(secret);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve conter pelo menos 32 bytes em Base64");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(bytes, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();

        OAuth2TokenValidator<Jwt> requiredClaims = jwt -> {
            if (jwt.getSubject() == null || jwt.getSubject().isBlank() || jwt.getExpiresAt() == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "sub e exp obrigatorios", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        OAuth2TokenValidator<Jwt> expiration = jwt -> {
            if (jwt.getExpiresAt() != null && !jwt.getExpiresAt().isAfter(Instant.now())) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("token_expired", "Token expirado", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        // Sem tolerancia extra para tokens expirados, conforme o criterio da tarefa.
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), requiredClaims, expiration));
        return decoder;
    }
}
