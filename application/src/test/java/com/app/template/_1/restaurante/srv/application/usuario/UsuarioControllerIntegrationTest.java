package com.app.template._1.restaurante.srv.application.usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(UsuarioControllerIntegrationTest.ProbeConfiguration.class)
class UsuarioControllerIntegrationTest {
    @Autowired MockMvc mvc;
    @MockitoSpyBean UsuarioController controller;
    @Autowired UsuarioRepository repository;
    @Value("${security.jwt.secret}") String secret;
    private Long usuarioId;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        usuarioId = repository.save(new Usuario("Joao", "joao@exemplo.com")).getId();
        repository.save(new Usuario("Outra pessoa", "outro@exemplo.com"));
    }

    @Test
    void tokenValidoRetornaSomenteUsuarioAutenticado() throws Exception {
        mvc.perform(get("/api/usuarios/me?email=outro@exemplo.com")
                .header("Authorization", "Bearer " + token("joao@exemplo.com", 3600L, secret, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioId.intValue()))
                .andExpect(jsonPath("$.nome").value("Joao"))
                .andExpect(jsonPath("$.email").value("joao@exemplo.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
    @Test void expiradoRetorna403() throws Exception {
        chamada(token("joao@exemplo.com", -1L, secret, null), 403);
    }
    @Test void assinaturaErradaRetorna401MesmoComExpiracaoPassada() throws Exception {
        chamada(token("joao@exemplo.com", -3600L,
                Base64.getEncoder().encodeToString(new byte[32]), null), 401);
    }
    @Test void tokenMalformadoRetorna401() throws Exception { chamada("nao-e-um-jwt", 401); }
    @Test void semEmailRetorna401() throws Exception { chamada(token(null, 3600L, secret, null), 401); }
    @Test void semExpiracaoRetorna401() throws Exception {
        chamada(token("joao@exemplo.com", null, secret, null), 401);
    }
    @Test void aindaNaoValidoRetorna401() throws Exception {
        chamada(token("joao@exemplo.com", 3600L, secret, Instant.now().plusSeconds(600)), 401);
    }
    @Test void usuarioInexistenteRetorna401() throws Exception {
        chamada(token("ausente@exemplo.com", 3600L, secret, null), 401);
    }
    @ParameterizedTest
    @EnumSource(Cargo.class)
    void contextoContemTipoEmailECargoDoBanco(Cargo cargo) throws Exception {
        repository.deleteAll();
        repository.save(new Usuario("Joao", "joao@exemplo.com", cargo));
        mvc.perform(get("/teste/contexto").header("Authorization",
                "Bearer " + token("joao@exemplo.com", 3600L, secret, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("UsernamePasswordAuthenticationToken"))
                .andExpect(jsonPath("$.email").value("joao@exemplo.com"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_" + cargo.name()));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        mvc.perform(get("/teste/contexto")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login", "/swagger-ui.html", "/swagger-ui/index.html",
            "/api/docs", "/api/docs/swagger-config", "/v3/api-docs", "/v3/api-docs/swagger-config"})
    void rotasPublicasIgnoramAusenciaETokenInvalido(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get(path).header("Authorization", "Bearer invalido"))
                .andExpect(status().isOk());
    }

    @Test
    void loginAceitaPostSemAutenticacao() throws Exception {
        mvc.perform(post("/api/auth/login")).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login-extra", "/api/docs-extra", "/swagger-ui.html-extra",
            "/api/usuarios/me", "/qualquer-rota"})
    void demaisRotasExigemToken(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        verify(controller, never()).me();
    }

    @Test
    void corsAceitaPreflightAngularSemToken() throws Exception {
        mvc.perform(options("/api/usuarios/me")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
        verify(controller, never()).me();
    }

    @Test
    void corsRetornaCabecalhosNaRespostaAutenticadaENoErro() throws Exception {
        mvc.perform(get("/api/usuarios/me").header("Origin", "http://localhost:4200")
                .header("Authorization", "Bearer " + token("joao@exemplo.com", 3600L, secret, null)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        mvc.perform(get("/api/usuarios/me").header("Origin", "http://localhost:4200"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void corsRecusaOrigemNaoAutorizada() throws Exception {
        mvc.perform(options("/api/usuarios/me").header("Origin", "http://outro-site.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        verify(controller, never()).me();
    }

    @Test
    void claimsAusentesTemPrioridadeSobreExpiracao() throws Exception {
        chamada(token(null, -60L, secret, null), 401);
    }

    @Test
    void tokenNaQueryNaoSubstituiHeader() throws Exception {
        mvc.perform(get("/api/usuarios/me").param("access_token",
                token("joao@exemplo.com", 3600L, secret, null)))
                .andExpect(status().isUnauthorized());
        verify(controller, never()).me();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ProbeConfiguration {
        @Bean ProbeController probeController() { return new ProbeController(); }
    }

    // Rotas artificiais apenas nos testes: provam a politica do filtro, nao implementam login/Swagger.
    @RestController
    static class ProbeController {
        @RequestMapping("/teste/contexto")
        Map<String, Object> contexto() {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return Map.of("tipo", auth.getClass().getSimpleName(), "email", auth.getName(),
                    "authorities", auth.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        }

        @RequestMapping({"/api/auth/login", "/swagger-ui.html", "/swagger-ui/index.html",
                "/api/docs", "/api/docs/swagger-config", "/v3/api-docs", "/v3/api-docs/swagger-config"})
        Map<String, String> publico() { return Map.of("resultado", "publico"); }
    }

    private void chamada(String token, int expectedStatus) throws Exception {
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
        if (expectedStatus == 401 || expectedStatus == 403) verify(controller, never()).me();
    }
    private String token(String email, Long segundos, String chave, Instant notBefore) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().subject(email).claim("cargo", "ADMIN")
                .issueTime(Date.from(Instant.now().minusSeconds(7200)));
        if (segundos != null) claims.expirationTime(Date.from(Instant.now().plusSeconds(segundos)));
        if (notBefore != null) claims.notBeforeTime(Date.from(notBefore));
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(Base64.getDecoder().decode(chave)));
        return jwt.serialize();
    }
}
