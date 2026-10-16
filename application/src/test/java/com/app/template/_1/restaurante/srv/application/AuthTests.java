package com.app.template._1.restaurante.srv.application;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.security.user.name=tester",
    "spring.security.user.password=test-password",
    "spring.security.user.roles=USER"
})
@AutoConfigureMockMvc
class AuthTests {

    @Autowired
    private MockMvc mvc;

    @Value("${local.server.port}")
    private int port;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void realHttpPreservesValidationAndCsrfErrorCodes() throws Exception {
        var base = "http://localhost:" + port;
        try (var client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build()) {
            var tokenResponse = client.send(HttpRequest.newBuilder(URI.create(base + "/api/auth/csrf")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, tokenResponse.statusCode());
            var token = mapper.readTree(tokenResponse.body()).get("token").asString();
            var invalidRequest = HttpRequest.newBuilder(URI.create(base + "/api/auth/login"))
                    .header("Content-Type", "application/json").header("X-CSRF-TOKEN", token)
                    .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"\",\"password\":\"\"}")).build();
            assertEquals(400, client.send(invalidRequest, HttpResponse.BodyHandlers.ofString()).statusCode());

            var missingCsrf = HttpRequest.newBuilder(URI.create(base + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}")).build();
            assertEquals(403, client.send(missingCsrf, HttpResponse.BodyHandlers.ofString()).statusCode());
        }
    }

    @Test
    void loginPersistsIdentityRotatesSessionAndSupportsLogout() throws Exception {
        var csrf = csrf(new MockHttpSession());
        var oldSessionId = csrf.session().getId();
        login(csrf, "test-password").andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
        assertNotEquals(oldSessionId, csrf.session().getId());

        mvc.perform(get("/api/usuarios/me").session(csrf.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(post("/api/auth/logout").session(csrf.session()).header(csrf.header(), csrf.token()))
                .andExpect(status().isForbidden());
        var refreshed = csrf(csrf.session());
        mvc.perform(post("/api/auth/logout").session(refreshed.session())
                        .header(refreshed.header(), refreshed.token()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidPasswordDoesNotCreateAuthenticatedSession() throws Exception {
        var csrf = csrf(new MockHttpSession());
        login(csrf, "wrong-password").andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios/me").session(csrf.session())).andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousMeIsUnauthorized() throws Exception {
        mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginRequiresCsrf() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"test-password\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyCredentialsAreBadRequest() throws Exception {
        var csrf = csrf(new MockHttpSession());
        mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions login(CsrfSession csrf, String password)
            throws Exception {
        return mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.header(), csrf.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of("username", "tester", "password", password))));
    }

    private CsrfSession csrf(MockHttpSession session) throws Exception {
        var result = mvc.perform(get("/api/auth/csrf").session(session))
                .andExpect(status().isOk()).andReturn();
        var json = mapper.readTree(result.getResponse().getContentAsString());
        var actualSession = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(actualSession);
        return new CsrfSession(actualSession, json.get("headerName").asString(), json.get("token").asString());
    }

    private record CsrfSession(MockHttpSession session, String header, String token) {
    }
}
