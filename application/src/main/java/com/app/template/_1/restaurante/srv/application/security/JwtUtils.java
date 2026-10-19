package com.app.template._1.restaurante.srv.application.security;

import java.util.List;
import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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
    private long expirationMillis;

    private SecretKey getSigningKey() {
        byte[] key = Decoders.BASE64.decode(secret);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve decodificar para pelo menos 32 bytes");
        }
        return Keys.hmacShaKeyFor(key);
    }

    /** Gera um token JWT para o usuário informado. */
    public String generateToken(UserDetails userDetails) {
        if (expirationMillis <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION deve ser maior que zero");
        }

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("authorities", authorities)
                .issuedAt(now)
                .expiration(expiration)
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
}
