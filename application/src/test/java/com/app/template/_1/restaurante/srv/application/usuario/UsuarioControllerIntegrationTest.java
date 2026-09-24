package com.app.template._1.restaurante.srv.application.usuario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class UsuarioControllerIntegrationTest {
    @Autowired MockMvc mvc;
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
    @Test void usuarioInexistenteRetorna404() throws Exception {
        chamada(token("ausente@exemplo.com", 3600L, secret, null), 404);
    }
    private void chamada(String token, int expectedStatus) throws Exception {
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }
    private String token(String email, Long segundos, String chave, Instant notBefore) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().subject(email)
                .issueTime(Date.from(Instant.now().minusSeconds(7200)));
        if (segundos != null) claims.expirationTime(Date.from(Instant.now().plusSeconds(segundos)));
        if (notBefore != null) claims.notBeforeTime(Date.from(notBefore));
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(Base64.getDecoder().decode(chave)));
        return jwt.serialize();
    }
}
