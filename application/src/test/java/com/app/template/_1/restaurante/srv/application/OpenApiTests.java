package com.app.template._1.restaurante.srv.application;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@AutoConfigureMockMvc
@Import(OpenApiTests.ExampleController.class)
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
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }

    @Test
    void openApiListsOperationsAndAuthentication() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API do Restaurante"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.security[0].basicAuth").isArray())
                .andExpect(jsonPath("$.paths['/test/example/{id}'].get.summary").value("Consultar exemplo"))
                .andExpect(jsonPath("$.paths['/test/example/{id}'].get.parameters[0].example").value("1"))
                .andExpect(jsonPath("$.paths['/test/example/{id}'].get.responses['200'].description")
                        .value("Exemplo encontrado"));
    }

    @Test
    void applicationEndpointsStillRequireAuthentication() throws Exception {
        mvc.perform(get("/test/example/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/test/example/1").with(user("tester")))
                .andExpect(status().isOk());
    }

    @RestController
    static class ExampleController {
        @Operation(summary = "Consultar exemplo")
        @ApiResponse(responseCode = "200", description = "Exemplo encontrado")
        @GetMapping("/test/example/{id}")
        String example(@Parameter(description = "Identificador", example = "1") @PathVariable Long id) {
            return id.toString();
        }
    }
}
