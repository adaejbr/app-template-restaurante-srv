# Estrutura e perfis — INFRA-01

Novos módulos seguem `com.ada.erp.<modulo>`, com `controller`, `service`,
`repository`, `domain`, `dto`, `mapper`, `exception`, `config` e `specification`.
Os pacotes versionados com `package-info.java` documentam cada responsabilidade.
Estão preparados os módulos `restaurante`, `autenticacao`, `usuario` e `mensageria`.

Controller valida DTOs e delega ao service; service aplica regras e delimita a
transação; repository acessa o banco. Domain não depende de controller/DTO.
Mapper converte DTO/domain; specification compõe filtros JPA. Shared contém apenas
recursos transversais, sem dependência de módulos: validation, util, exception e config.

O código anterior permanece em `com.app.restaurante.srv.application` nesta entrega.
`ErpModulesConfig` registra componentes, entidades e repositórios dos dois namespaces,
permitindo migrar cada módulo sem alterar simultaneamente autenticação e demais APIs.
Não crie cópias de entidades/repositórios: mova cada implementação quando seu módulo for migrado.

## Perfis

| Perfil | Datasource | JWT | Flyway/JPA | Logging |
| --- | --- | --- | --- | --- |
| dev (padrão) | MySQL local; DB_URL ou DB_NAME, DB_USERNAME/DB_USER e DB_PASSWORD | JWT_SECRET obrigatório em Base64, ao menos 32 bytes | migrations + validate; open-in-view=false | aplicação DEBUG; restante INFO |
| test | H2 isolado, dependência de teste | chave exclusiva de teste | Flyway desativado; create-drop exclusivamente para testes isolados | raiz WARN |
| prod | DB_URL, DB_USERNAME/DB_USER e DB_PASSWORD obrigatórios | JWT_SECRET obrigatório em Base64, ao menos 32 bytes | migrations + validate; open-in-view=false | INFO; SQL WARN |

JWT_EXPIRATION controla a validade em milissegundos (padrão 1800000).
O perfil test usa o classpath de testes (`./mvnw spring-boot:run -Dspring-boot.run.useTestClasspath=true`).
Não use test em produção. O perfil local anterior é legado; use dev para executar com banco.
Flyway valida checksums e proíbe clean. V1 e V2 não foram editadas;
V3 cria `usuarios`, entidade já existente que impedia o startup com validate.

## Executar com MySQL local

Na raiz, configure .env conforme docker-compose.yml. Exporte DB_PASSWORD e use
DB_NAME/DB_USER iguais aos do Compose. DB_USERNAME tem precedência sobre DB_USER.

```powershell
docker compose up -d mysql
cd application
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DB_NAME = 'restaurante'
$env:DB_USER = 'rest_user'
$env:DB_PASSWORD = '<senha-do-MySQL>'
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

É necessário JDK 25. JWT_SECRET é obrigatória em dev e prod, sem valor padrão;
use uma chave própria para cada ambiente. Em prod, configure
SPRING_PROFILES_ACTIVE=prod e todas as credenciais no ambiente. Não versione as chaves.

## Validar

```powershell
cd application
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
# Banco exclusivo e inicialmente vazio, sem reutilizar o banco de desenvolvimento:
$env:MYSQL_TEST_URL = 'jdbc:mysql://localhost:3307/infra_test'
$env:MYSQL_TEST_USER = 'infra_test'
$env:MYSQL_TEST_PASSWORD = 'infra_test'
.\mvnw.cmd --batch-mode --no-transfer-progress -Pmysql-integration clean verify
```

O primeiro comando executa testes isolados sem serviços externos. O segundo inclui
InfrastructureMySqlIT: contexto real, MySQL, três migrations, todas as entidades
validadas, BCrypt do seed e reaplicação idempotente do Flyway. Não usa H2 como
evidência de compatibilidade MySQL. O workflow Integração MySQL fornece um banco
novo em cada job e executa o mesmo comando. O seed legado é preservado; aplicação
vazia significa startup sobre schema inicialmente vazio, sem chamadas à API.
