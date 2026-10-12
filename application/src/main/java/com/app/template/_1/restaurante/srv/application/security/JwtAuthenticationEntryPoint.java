package com.app.template._1.restaurante.srv.application.security;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        boolean expired = false;
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
    }
}
