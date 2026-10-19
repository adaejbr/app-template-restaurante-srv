# Autenticação JWT

A API autentica funcionários por e-mail e senha em `POST /api/auth/login` e emite um token JWT assinado com HS256. O Spring Security OAuth2 Resource Server valida esse token nas rotas protegidas.

## Login

Envie:

```json
{
  "email": "gerente@restaurante.com",
  "senha": "admin123"
}
```

Resposta `200`:

```json
{
  "token": "<JWT>",
  "userId": 1,
  "nome": "Gerente Padrao",
  "cargo": "GERENTE"
}
```

O `userId` e o nome vêm do funcionário e do perfil associado. O cargo é o primeiro cargo associado ao funcionário com status `ATIVO`; sem cargo ativo, a resposta contém `null` nesse campo. Credenciais inválidas ou funcionário inativo retornam `401`; e-mail/senha ausentes ou malformados retornam `400`.

Para chamadas autenticadas, envie o token no cabeçalho:

```http
Authorization: Bearer <JWT>
```

Por exemplo:

```powershell
curl.exe http://localhost:8080/api/usuarios/me `
  -H "Authorization: Bearer $token"
```

Os tokens contêm `sub` com o login do funcionário, as authorities, `iat` e `exp`. A validade padrão é de 30 minutos.

## Configuração

Defina `JWT_SECRET` como uma chave aleatória de pelo menos 32 bytes codificada em Base64. `JWT_EXPIRATION` é opcional e representa milissegundos; o padrão é `1800000` (30 minutos).

Exemplo para gerar uma chave local no PowerShell:

```powershell
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:JWT_EXPIRATION = '1800000'
```

Não grave a chave em código, arquivo versionado ou logs. O emissor (`JwtUtils`) e o Resource Server usam os mesmos bytes decodificados da chave para assinar e validar HS256.

## Senhas

A migration de desenvolvimento atual insere `admin123` em texto plano e o encoder do login mantém compatibilidade com esses registros. Não use esse armazenamento em produção. O uso de BCrypt exige migração das senhas existentes e mudança coordenada do `PasswordEncoder`.

## CORS

O CORS permite a origem `http://localhost:4200`, métodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`, além dos cabeçalhos `Accept`, `Authorization` e `Content-Type`. A autenticação usa token Bearer, não cookies de sessão.

## Testes

Na raiz do repositório:

```powershell
.\application\mvnw.cmd -f application\pom.xml -Dtest=AuthTests test
.\application\mvnw.cmd -f application\pom.xml -Dtest=AuthenticationControllerTest test
.\application\mvnw.cmd -f application\pom.xml test
```

Os testes verificam emissão do JWT e dados do funcionário, credenciais inválidas, bloqueio de funcionário inativo, campos obrigatórios, autenticação Bearer em rota protegida e documentação OpenAPI.

`AuthenticationControllerTest` usa `@WebMvcTest` com os repositórios mockados para verificar respostas `200`, `400` e `401`, mantendo os filtros de segurança ativos. `AuthTests` mantém o smoke test com o contexto completo e a emissão/validação real do JWT.

O [guia de CI, Checkstyle e testes](ci-testes.md) descreve os pré-requisitos, a configuração do plugin e um exemplo com MockMvc.
