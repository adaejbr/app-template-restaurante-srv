package com.app.template._1.restaurante.srv.application.security;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import com.app.template._1.restaurante.srv.application.usuario.Usuario;
import com.app.template._1.restaurante.srv.application.usuario.UsuarioRepository;

// Registrado somente na SecurityFilterChain, nunca como filtro global do servlet.
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtDecoder decoder;
    private final UsuarioRepository usuarios;
    private final AuthenticationEntryPoint entryPoint;
    private final RequestMatcher publicEndpoints;
    private final DefaultBearerTokenResolver tokenResolver = new DefaultBearerTokenResolver();

    public JwtAuthenticationFilter(JwtDecoder decoder, UsuarioRepository usuarios,
            AuthenticationEntryPoint entryPoint, RequestMatcher publicEndpoints) {
        this.decoder = decoder;
        this.usuarios = usuarios;
        this.entryPoint = entryPoint;
        this.publicEndpoints = publicEndpoints;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return publicEndpoints.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        try {
            String token = tokenResolver.resolve(request);
            if (token == null) {
                throw new BadCredentialsException("Token ausente");
            }
            Jwt jwt = decoder.decode(token);
            Usuario usuario = usuarios.findByEmail(jwt.getSubject())
                    .orElseThrow(() -> new BadCredentialsException("Usuario inexistente"));
            // O cargo vem do banco, nao de claims de permissao controlados pelo cliente.
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getCargo().name()));
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    usuario.getEmail(), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (JwtException exception) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, new BadCredentialsException("Token invalido", exception));
            return;
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, exception);
            return;
        }

        // Fora do catch: erros de negocio do controller nao viram erros de autenticacao.
        chain.doFilter(request, response);
    }
}
