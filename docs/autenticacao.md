# Autenticação por sessão

A API usa sessão HTTP (`JSESSIONID`) para autenticação e proteção CSRF para requisições que alteram estado. O fluxo é implementado em `AuthController` e na configuração de segurança do módulo `application`.

## Fluxo

1. Faça `GET /api/auth/csrf`. A resposta contém `headerName` e `token`; mantenha também o cookie de sessão recebido.
2. Envie `POST /api/auth/login` com o cookie, o token no cabeçalho indicado e as credenciais JSON:

   ```json
   {"username":"user","password":"senha"}
   ```

3. O login retorna `username` e `authorities` e gira o identificador da sessão. O token CSRF anterior deixa de ser válido.
4. Use a sessão para chamar `GET /api/usuarios/me`. Para logout, obtenha um token CSRF atualizado e envie-o em `POST /api/auth/logout`.

O login usa o `AuthenticationManager` configurado pelo Spring Security. Em desenvolvimento/testes, as propriedades `spring.security.user.name`, `spring.security.user.password` e `spring.security.user.roles` configuram o usuário padrão; em produção, configure um provedor de autenticação apropriado e credenciais fora do repositório.

## Proteção e respostas

- `GET /api/auth/csrf`, login e documentação OpenAPI/Swagger são públicos. As demais rotas exigem sessão autenticada.
- CSRF permanece habilitado para métodos mutáveis; token ausente ou inválido retorna `403`.
- Sessão ausente ou credenciais inválidas retornam `401`.
- O logout invalida a sessão e retorna `204`.
- `/api/usuarios/me` retorna somente `username` e `authorities`; não expõe senha.
- Cabeçalhos `Authorization: Bearer` não autenticam chamadas. Não há configuração JWT ativa.

## CORS e frontend

O CORS permite a origem `http://localhost:4200`, os métodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`, e os cabeçalhos `Accept`, `Content-Type` e `X-CSRF-TOKEN`. Credenciais CORS estão habilitadas para que o navegador envie o cookie de sessão. O frontend deve enviar as requisições com credenciais e conservar o cookie entre chamadas. Outras origens são recusadas.

## Testes

Execute a classe de integração e a suíte do módulo a partir da raiz do repositório:

```powershell
.\application\mvnw.cmd -f application\pom.xml -Dtest=UsuarioControllerIntegrationTest test
.\application\mvnw.cmd -f application\pom.xml test
```

Os testes de integração cobrem sessão, rotação de sessão, login, CSRF, logout, acesso a `/api/usuarios/me` e CORS.
