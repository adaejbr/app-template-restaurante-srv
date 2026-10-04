# API do Restaurante

Spring Boot 4.1, Java 25 e springdoc-openapi 3.1.1.

## Executar localmente

Na pasta `application`, use Maven instalado (`mvn`) ou o Wrapper (`.\mvnw.cmd`
no Windows, `bash mvnw` no Linux/macOS).

Para testar Swagger e autenticação sem configurar MySQL:

```powershell
cd application
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

O perfil `local` desativa a configuração automática do banco. Sem esse perfil,
configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
`SPRING_DATASOURCE_PASSWORD` para o seu MySQL.

- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [Swagger UI — endereço direto](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [OpenAPI YAML](http://localhost:8080/v3/api-docs.yaml)

## Autenticação e exemplos no Swagger

Esta etapa usa o usuário **em memória** fornecido pelo Spring Security. Não há
cadastro persistente ou emissão de JWT. Por padrão, o nome é `user` e a senha
temporária é mostrada no log de inicialização. Para configurar credenciais,
defina `SPRING_SECURITY_USER_NAME` e `SPRING_SECURITY_USER_PASSWORD` no ambiente
antes de iniciar. Não salve senhas no repositório.

1. Abra o Swagger e execute `GET /api/auth/csrf` em **Try it out**.
2. Copie o campo `token` da resposta.
3. Em `POST /api/auth/login`, cole esse valor no parâmetro `X-CSRF-TOKEN` e
   preencha o JSON com o usuário e a senha da aplicação:

   ```json
   { "username": "user", "password": "substitua-pela-sua-senha" }
   ```

4. Execute o login. A resposta `200` retorna o usuário e suas permissões; o
   navegador recebe o cookie de sessão `JSESSIONID` automaticamente.
5. Execute `GET /api/usuarios/me` no mesmo navegador. Exemplo de resposta:

   ```json
   { "username": "user", "authorities": ["FACTOR_PASSWORD"] }
   ```

O esquema `sessionAuth` documenta o cookie em `@SecurityScheme`. Não é necessário
preencher **Authorize**: o login cria a sessão, e o navegador envia o cookie nas
requisições seguintes. Os endpoints de login e consulta do usuário têm
`@Operation`, exemplos e respostas documentadas. Credenciais incorretas retornam
`401`; campos vazios retornam `400` quando o token CSRF é válido; token CSRF
ausente ou inválido retorna `403`; consulta sem sessão retorna `401`.

As permissões dependem da configuração do usuário: `SPRING_SECURITY_USER_ROLES`
permite definir papéis como `USER` (exposto como `ROLE_USER`). O Spring Security
também identifica a autenticação por senha com `FACTOR_PASSWORD`.

Clientes HTTP devem guardar os cookies recebidos e reenviá-los nas próximas
requisições. A proteção CSRF permanece ativa. Após login ou logout, obtenha um
novo token em `/api/auth/csrf` antes de fazer outra operação de escrita.
O Spring Security também atende `POST /api/auth/logout`: envie o token atualizado
em `X-CSRF-TOKEN` e o cookie da sessão; a resposta é `204`.

## Compilação, testes e linting

Execute a partir de `application`:

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd checkstyle:check
.\mvnw.cmd clean verify
```

Os mesmos objetivos funcionam com `mvn`. O plugin Checkstyle 3.6.0 roda na fase
`validate`, antes da compilação e dos testes, e falha o build em qualquer violação.
As regras em `application/checkstyle.xml` verificam nomes, imports, chaves,
comprimento de linha e erros como statements vazios e contratos equals/hashCode,
incluindo código de testes.

Os testes usam H2 somente no escopo de teste, com o contexto de persistência
habilitado; nenhum MySQL externo é necessário. Eles verificam inicialização,
Swagger público, documentação dos endpoints reais, credenciais válidas e
inválidas, validação do JSON, sessão, rotação do identificador da sessão, CSRF
e logout. O perfil `local` não é usado pelos testes.

## Pipeline

`.github/workflows/build.yml` executa `clean verify` com Java 25 em pushes e
pull requests no GitHub. Essa execução reúne Checkstyle, compilação, testes e
empacotamento. O pipeline remoto começará a rodar quando as alterações forem
enviadas ao GitHub.

## Organização

O pacote raiz é `com.app.template._1.restaurante.srv.application`, com subpacotes
`auth`, `usuario` e `config`. A classe `Application` fica no diretório correspondente
ao pacote, para que o component scan encontre os controllers e configurações.
