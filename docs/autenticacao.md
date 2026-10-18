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

A migration inicial insere o gerente `gerente@restaurante.com` com a senha `admin123`. Ao iniciar a aplicação, o `DataInitializer` busca esse funcionário e salva um hash BCrypt de `admin123` se a senha ainda não estiver hasheada. A rotina executa em uma transação e preserva hashes BCrypt existentes, inclusive de senhas alteradas, nas próximas inicializações. Se o gerente não existir, a rotina não cria outro cadastro.

O login utiliza `BCryptPasswordEncoder` para validar a senha informada. A conversão automática se limita ao gerente do seed; outros funcionários precisam ter senhas BCrypt para autenticar. A senha inicial é destinada ao ambiente de desenvolvimento e deve ser alterada em ambientes reais.

Para validar o J-11, inicie a aplicação com o seed, confira o hash na coluna `funcionario.senha` e faça login com `admin123`. Reinicie e confirme que o hash não mudou. Uma senha incorreta deve retornar `401`.

## CORS

O CORS permite a origem `http://localhost:4200`, métodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`, além dos cabeçalhos `Accept`, `Authorization` e `Content-Type`. A autenticação usa token Bearer, não cookies de sessão.

## Testes

Na raiz do repositório:

```powershell
.\application\mvnw.cmd -f application\pom.xml -Dtest=AuthTests test
.\application\mvnw.cmd -f application\pom.xml -Dtest=DataInitializerTests test
.\application\mvnw.cmd -f application\pom.xml test
```

Os testes verificam emissão do JWT e dados do funcionário, credenciais inválidas, bloqueio de funcionário inativo, campos obrigatórios, autenticação Bearer em rota protegida e documentação OpenAPI.

`DataInitializerTests` usa H2 com o script real `V2__seed_data.sql` carregado antes dos runners. Verifica o hash persistido na inicialização, login com o gerente, senha incorreta, reexecução sem alterar o hash, preservação de senhas alteradas nos formatos BCrypt `$2a$`, `$2b$` e `$2y$` e ausência do gerente sem modificar outro funcionário. Nesse teste, o Hibernate cria o schema e o Flyway fica desativado; a execução das migrations no MySQL não é coberta.
