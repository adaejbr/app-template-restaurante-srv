# App_Template_01_Restaurante_Backend

## Swagger / OpenAPI

Requisitos: Java 25 e as dependências baixadas pelo Maven Wrapper.
O projeto usa Spring Boot 4.1 e `springdoc-openapi-starter-webmvc-ui` 3.1.1.

Na pasta `application`, execute no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A inicialização normal exige configurar o MySQL (`spring.datasource.url`,
`spring.datasource.username` e `spring.datasource.password`). Como esta branch
ainda não contém entidades ou endpoints de negócio, é possível visualizar apenas
a documentação sem banco de dados:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
```

- Swagger UI: http://localhost:8080/swagger-ui.html ou http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

A documentação é pública. Os demais caminhos continuam exigindo autenticação.
Em **Authorize**, use HTTP Basic com as credenciais do Spring Security. Sem uma
configuração própria de usuários, o usuário é `user` e a senha temporária aparece
no log de inicialização. Não há autenticação JWT nesta branch.

A proteção CSRF continua habilitada: futuras operações POST, PUT, PATCH e DELETE
também precisarão de um token CSRF válido, além da autenticação.

### Documentação dos endpoints

Esta branch ainda não possui controllers de negócio; por isso, a UI inicialmente
não exibe operações. Controllers criados dentro do pacote
`com.app.template._1.restaurante.srv.application` (ou subpacotes) serão listados
automaticamente. O exemplo abaixo é apenas uma referência para um futuro controller:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Operation(summary = "Buscar restaurante", description = "Consulta um restaurante pelo identificador.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Restaurante encontrado"),
    @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content),
    @ApiResponse(responseCode = "404", description = "Restaurante não encontrado", content = @Content)
})
@GetMapping("/restaurantes/{id}")
public RestauranteResponse buscar(
        @Parameter(description = "Identificador do restaurante", example = "1")
        @PathVariable Long id) {
    return restauranteService.buscar(id);
}
```

`RestauranteResponse` e `restauranteService` ilustram componentes futuros.
`@SecurityScheme` está definido em `OpenApiConfig`, e a exigência de HTTP Basic
é documentada globalmente. Para uma operação pública, use
`@SecurityRequirements` (de `io.swagger.v3.oas.annotations.security`) sem valores
no método e libere também o caminho em `SecurityConfig`;
as annotations OpenAPI não alteram as regras reais de acesso.

### Validação

```powershell
cd application
.\mvnw.cmd test
```

Os testes desabilitam apenas a configuração automática do banco, verificam o
acesso público à UI e ao JSON, a documentação de um controller exclusivo dos
testes e a exigência de autenticação nos endpoints da aplicação.
