package com.app.template._1.restaurante.srv.application.usuario;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
    "spring.security.user.name=joao@exemplo.com",
    "spring.security.user.password=integration-password",
    "spring.security.user.roles=USER"
})
@AutoConfigureMockMvc
class UsuarioControllerIntegrationTest {
    private static final String USERNAME = "joao@exemplo.com";
    private static final String PASSWORD = "integration-password";
    private static final String ANGULAR_ORIGIN = "http://localhost:4200";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockitoSpyBean
    UsuarioController controller;

    @Test
    void loginCriaSessaoEPermiteConsultarUsuarioAutenticado() throws Exception {
        CsrfSession csrf = csrf();
        String oldSessionId = csrf.session().getId();

        login(csrf, USERNAME, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertNotEquals(oldSessionId, csrf.session().getId());
        mvc.perform(get("/api/usuarios/me").session(csrf.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rotaMeExigeSessaoEIgnoraTokenBearer() throws Exception {
        mvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
        verify(controller, never()).me();
    }

    @Test
    void loginExigeTokenCsrf() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD))))
                .andExpect(status().isForbidden());
    }

    @Test
    void credenciaisInvalidasNaoCriamSessaoAutenticada() throws Exception {
        CsrfSession csrf = csrf();
        login(csrf, USERNAME, "senha-incorreta")
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/usuarios/me").session(csrf.session()))
                .andExpect(status().isUnauthorized());
        verify(controller, never()).me();
    }

    @Test
    void logoutExigeCsrfAtualEEncerraSessao() throws Exception {
        CsrfSession csrf = csrf();
        login(csrf, USERNAME, PASSWORD).andExpect(status().isOk());

        mvc.perform(post("/api/auth/logout")
                        .session(csrf.session())
                        .header(csrf.header(), csrf.token()))
                .andExpect(status().isForbidden());

        CsrfSession refreshed = csrf(csrf.session());
        mvc.perform(post("/api/auth/logout")
                        .session(refreshed.session())
                        .header(refreshed.header(), refreshed.token()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPermitePreflightComCookiesEHeaderCsrf() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", ANGULAR_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-csrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void corsIncluiCabecalhosNasRespostasAutenticadasENaoAutenticadas() throws Exception {
        CsrfSession csrf = csrf();
        login(csrf, USERNAME, PASSWORD).andExpect(status().isOk());

        mvc.perform(get("/api/usuarios/me")
                        .session(csrf.session())
                        .header("Origin", ANGULAR_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

        mvc.perform(get("/api/usuarios/me").header("Origin", ANGULAR_ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void corsRecusaOrigemNaoAutorizada() throws Exception {
        mvc.perform(options("/api/usuarios/me")
                        .header("Origin", "http://outro-site.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        verify(controller, never()).me();
    }

    private ResultActions login(CsrfSession csrf, String username, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .session(csrf.session())
                .header(csrf.header(), csrf.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", username, "password", password))));
    }

    private CsrfSession csrf() throws Exception {
        return csrf(null);
    }

    private CsrfSession csrf(MockHttpSession session) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/auth/csrf");
        if (session != null) {
            request.session(session);
        }
        MvcResult result = mvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var json = mapper.readTree(result.getResponse().getContentAsString());
        var actualSession = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(actualSession);
        return new CsrfSession(actualSession, json.get("headerName").asString(), json.get("token").asString());
    }

    private record CsrfSession(MockHttpSession session, String header, String token) {
    }
}
