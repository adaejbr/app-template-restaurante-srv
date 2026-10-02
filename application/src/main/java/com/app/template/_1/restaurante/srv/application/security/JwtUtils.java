package com.app.template._1.restaurante.srv.application.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utilitário de geração/validação de tokens JWT (task j-05).
 *
 * NOTA: esta é uma implementação mínima criada pela j-05.
 * A task j-04 (Spring Security + JWT) deve revisar/expandir este componente.
 */
@Component
public class JwtUtils {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expirationMillis; // em milissegundos

	private SecretKey getSigningKey() {
		// HS256 exige chave >= 32 bytes
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/** Gera um token JWT para o usuário informado. */
	public String generateToken(UserDetails userDetails) {
		Date now = new Date();
		return Jwts.builder()
				.subject(userDetails.getUsername())
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMillis))
				.signWith(getSigningKey(), Jwts.SIG.HS256)
				.compact();
	}

	/** Extrai o username (login) contido no token. */
	public String extractUsername(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	/** Valida o token: assinatura válida, não expirado e username confere. */
	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			String username = extractUsername(token);
			return username.equals(userDetails.getUsername());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}
