package com.app.template._1.restaurante.srv.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void swaggerUiIsPublic() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        mvc.perform(get("/swagger-ui/swagger-ui-bundle.js")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }

    @Test
    void openApiDocumentsRealEndpointsAndExamples() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.info.title").value("API do Restaurante"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.summary")
                        .value("Autenticar funcionário e emitir JWT"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.requestBody.content"
                        + "['application/json'].schema['$ref']").value("#/components/schemas/LoginRequest"))
                .andExpect(jsonPath("$.components.schemas.LoginRequest.properties.email.example")
                        .value("gerente@restaurante.com"))
                .andExpect(jsonPath("$.components.schemas.LoginRequest.properties.senha").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['401']").exists())
                .andExpect(jsonPath("$.components.schemas.LoginResponse.properties.token").exists())
                .andExpect(jsonPath("$.components.schemas.LoginResponse.properties.userId").exists())
                .andExpect(jsonPath("$.components.schemas.LoginResponse.properties.nome").exists())
                .andExpect(jsonPath("$.components.schemas.LoginResponse.properties.cargo").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/me'].get.summary")
                        .value("Consultar usuário autenticado pelo JWT"))
                .andExpect(jsonPath("$.paths['/api/usuarios/me'].get.responses['200'].content"
                        + "['application/json'].example.username").value("user"))
                .andExpect(jsonPath("$.paths['/api/usuarios/me'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/test/example/{id}']").doesNotExist());
    }
}
