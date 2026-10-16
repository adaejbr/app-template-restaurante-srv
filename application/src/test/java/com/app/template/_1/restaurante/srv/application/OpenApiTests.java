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
                .andExpect(jsonPath("$.components.securitySchemes.sessionAuth.type").value("apiKey"))
                .andExpect(jsonPath("$.components.securitySchemes.sessionAuth.in").value("cookie"))
                .andExpect(jsonPath("$.components.securitySchemes.sessionAuth.name").value("JSESSIONID"))
                .andExpect(jsonPath("$.security[0].sessionAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.summary").value("Autenticar usuário"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.parameters[0].name").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['401']").exists())
                .andExpect(jsonPath("$.components.schemas.LoginRequest.properties.username.example").value("user"))
                .andExpect(jsonPath("$.components.schemas.LoginRequest.properties.password.example").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/me'].get.summary").value("Consultar usuário autenticado"))
                .andExpect(jsonPath("$.paths['/api/usuarios/me'].get.responses['200'].content"
                        + "['application/json'].example.username").value("user"))
                .andExpect(jsonPath("$.paths['/api/auth/csrf'].get.parameters").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/usuarios/me'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/test/example/{id}']").doesNotExist());
    }
}
