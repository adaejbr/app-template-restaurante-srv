package com.app.template._1.restaurante.srv.application.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API do Restaurante", version = "v1",
                description = "Autenticação por sessão. Obtenha o token em GET /api/auth/csrf, "
                        + "envie-o no login e consulte GET /api/usuarios/me."),
        security = @SecurityRequirement(name = "sessionAuth")
)
@SecurityScheme(name = "sessionAuth", type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE, paramName = "JSESSIONID",
        description = "Cookie criado pelo login. No Swagger, execute o login; "
                + "o navegador envia o cookie automaticamente nas próximas requisições.")
public class OpenApiConfig {
}
