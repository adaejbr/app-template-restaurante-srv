package com.app.template._1.restaurante.srv.application.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import com.app.template._1.restaurante.srv.application.usuario.UsuarioRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
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
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder decoder,
            UsuarioRepository usuarios) throws Exception {
        var paths = PathPatternRequestMatcher.withDefaults();
        RequestMatcher publicEndpoints = new OrRequestMatcher(
                paths.matcher("/api/auth/login"),
                paths.matcher("/swagger-ui.html"),
                paths.matcher("/swagger-ui/**"),
                paths.matcher("/api/docs"),
                paths.matcher("/api/docs/**"),
                paths.matcher("/v3/api-docs"),
                paths.matcher("/v3/api-docs/**"));
        AuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();
        var jwtFilter = new JwtAuthenticationFilter(decoder, usuarios, entryPoint, publicEndpoints);

        return http
                .cors(Customizer.withDefaults())
                // API Bearer sem autenticacao por cookies.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint))
                // O filtro nativo de Resource Server nao e registrado em paralelo.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:4200"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
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
        var timestamps = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), expiration);
        decoder.setJwtValidator(jwt -> {
            var required = requiredClaims.validate(jwt);
            // Claims obrigatorios invalidos tem prioridade sobre a expiracao.
            return required.hasErrors() ? required : timestamps.validate(jwt);
        });
        return decoder;
    }
}
