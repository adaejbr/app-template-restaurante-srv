# Autenticação: GET /api/usuarios/me

Entrega: consultar o usuário autenticado a partir de um JWT válido. Não houve commit.

## O que foi construído

| Arquivo em application/ | Responsabilidade |
| --- | --- |
| pom.xml | Adiciona OAuth2 Resource Server para validar JWT e H2 somente para testes. |
| src/main/resources/application.properties | Lê conexão MySQL e chave JWT de variáveis de ambiente; valida o esquema existente. |
| src/main/java/com/app/template/_1/restaurante/srv/application/usuario/Usuario.java | Entidade JPA da tabela usuarios: id, nome e email único. |
| .../usuario/UsuarioRepository.java | Usa Spring Data JPA para buscar por email. |
| .../usuario/UsuarioResponse.java | DTO: limita a resposta a id, nome e email. |
| .../usuario/UsuarioController.java | Implementa @GetMapping("/api/usuarios/me"), lê Authentication pelo SecurityContextHolder e busca o usuário. |
| .../security/SecurityConfig.java | Exige autenticação, valida HS256, sub, exp e nbf; responde 401 ou 403 conforme o caso. |
| src/test/resources/application.properties | Configura banco H2 e chave exclusiva de teste. |
| src/test/java/com/app/template/_1/restaurante/srv/application/usuario/UsuarioControllerIntegrationTest.java | Nove testes com JWTs assinados e acesso real ao banco em memória. |
| scripts/banco-local.sql | Cria banco/tabela e um usuário de exemplo, somente quando executado manualmente. |
| scripts/gerar-token-local.ps1 | Gera JWT para testes locais usando a mesma chave da aplicação. |
| README.md e docs/autenticacao.md | Orientações e explicação da entrega. |

## Como funciona

1. O cliente envia GET /api/usuarios/me com Authorization: Bearer TOKEN.
2. O filtro do Spring Security verifica a assinatura HS256 com JWT_SECRET.
3. O decoder exige sub (identidade) e exp (expiração), verifica nbf quando presente e rejeita tokens expirados sem tolerância adicional.
4. O Spring registra Authentication no contexto de segurança.
5. O controller chama SecurityContextHolder.getContext().getAuthentication().getName().
6. Nesse tipo de autenticação, getName() retorna sub; o contrato desta API exige que sub seja o email.
7. O repositório busca esse email no banco. Parâmetros como ?email=outra-pessoa não determinam a identidade.
8. O DTO devolve somente id, nome e email.

O principal do Spring é um objeto Jwt, não uma String. Por isso usamos getName(), sem converter getPrincipal() para String.
O DTO é um record Java, uma forma curta de declarar dados imutáveis.
O construtor do controller recebe o repositório automaticamente por injeção de dependência.

Todas as rotas exigem autenticação por padrão. Quando houver login, sua rota precisará de liberação explícita.
A aplicação não usa sessão nem cookies de autenticação. CSRF foi desativado para este modelo de API Bearer.

## Respostas

| Situação | HTTP |
| --- | --- |
| Token válido e usuário existente | 200 |
| Sem token | 401 |
| Token com assinatura válida, mas expirado | 403 |
| Token malformado, assinatura inválida ou sem sub/exp | 401 |
| Token com nbf no futuro | 401 |
| Token válido sem usuário no banco | 404 |

Exemplo de 200 (o id depende do banco):

```json
{
  "id": 1,
  "nome": "Joao",
  "email": "joao@exemplo.com"
}
```

Exemplo de token expirado:

```json
{"status":403,"erro":"Token expirado"}
```

O 403 para expiração é uma personalização exigida pela tarefa. Outros erros de autenticação permanecem 401.
A expiração só é classificada depois de verificar a assinatura: um token adulterado não ganha credibilidade por conter exp no passado.

## Executar com MySQL no Windows

Pré-requisitos: JDK 25, MySQL ativo e acesso à internet no primeiro uso do Maven Wrapper.
O Spring Boot 4.1.0 e Java 25 já eram as versões do projeto e foram preservados.

1. Abra scripts/banco-local.sql no MySQL Workbench e execute em um ambiente local.
   Ele cria restaurante, usuarios e o usuário joao@exemplo.com.
2. No PowerShell, entre na pasta application.
3. Configure as variáveis nesta janela:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/restaurante'
$env:DB_USERNAME = 'seu_usuario_mysql'
$env:DB_PASSWORD = 'sua_senha_mysql'

# Chave aleatória de 32 bytes, codificada em Base64.
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)

# Gere os tokens antes de iniciar o servidor.
$token = .\scripts\gerar-token-local.ps1
$expirado = .\scripts\gerar-token-local.ps1 -ValidadeSegundos -60
$token
$expirado

.\mvnw.cmd spring-boot:run
```

Copie os tokens exibidos para o Postman ou uma segunda janela.
Servidor e gerador precisam usar a mesma JWT_SECRET; gerar uma nova chave invalida tokens anteriores.
A configuração exige uma chave Base64 de pelo menos 32 bytes e não oferece segredo padrão de produção.
O gerador não envia a chave ou o token a serviços externos. É um utilitário de desenvolvimento; quem tem a chave pode gerar tokens.

O Hibernate usa ddl-auto=validate: verifica a tabela, sem criar ou modificar tabelas automaticamente.
As credenciais do banco e JWT_SECRET não ficam salvas no código.

## Testar com curl no PowerShell

Em outra janela, substitua COLE_O_TOKEN pelo token gerado anteriormente:

```powershell
$token = 'COLE_O_TOKEN_VALIDO'
$expirado = 'COLE_O_TOKEN_EXPIRADO'

# Esperado: 200 e dados de Joao
curl.exe -i http://localhost:8080/api/usuarios/me -H "Authorization: Bearer $token"

# Esperado: 401
curl.exe -i http://localhost:8080/api/usuarios/me

# Esperado: 403
curl.exe -i http://localhost:8080/api/usuarios/me -H "Authorization: Bearer $expirado"
```

Abrir a URL na barra do navegador normalmente não envia o cabeçalho Authorization e, portanto, retorna 401.

## Testar no Postman

1. Método GET, URL http://localhost:8080/api/usuarios/me.
2. Authorization > Bearer Token: cole o token válido sem o prefixo Bearer.
3. Send: espere 200 com id, nome e email.
4. Troque para No Auth: espere 401.
5. Volte a Bearer Token e cole o token expirado: espere 403.

## Testes automatizados e verificação realizada

Na pasta application:

```powershell
.\mvnw.cmd test
```

Resultado da execução desta entrega: BUILD SUCCESS, 10 testes, zero falhas e zero erros.
São nove testes novos de integração e o teste existente de inicialização.
Os testes exercitam o filtro, a assinatura JWT, o controller e a consulta JPA com H2.
Nenhuma credencial ou instância MySQL é necessária para esses testes.
A conexão com um MySQL real e os testes manuais pelo Postman não foram executados nesta entrega.

## Limites desta tarefa e integração futura

Esta entrega cobre o endpoint /me e sua validação de token. Não implementa cadastro, login com senha, recuperação de senha ou refresh token.
O futuro login deverá emitir JWT assinado em HS256 com a mesma chave, sub=email e exp.
Se a equipe escolher um provedor de identidade ou outro algoritmo, será necessário adaptar o decoder ao contrato dele.

Ainda não existe modelo de restaurantes/tenants no projeto. A implementação assume uma base de usuários com email globalmente único.
Ela não constitui isolamento multi-tenant para um sistema white-label com banco compartilhado. Esse isolamento depende da definição de restaurante e vínculo de usuário; não se deve liberar acesso a dados de restaurantes apenas por esta autenticação.

Referência usada: [Spring Security — JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
