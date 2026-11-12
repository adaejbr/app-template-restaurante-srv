# App_Template_01_Restaurante_Backend

Backend Spring Boot 4.1, Java 25 e MySQL 8, com autenticação JWT e schema gerenciado por Flyway.

- [Estrutura de pacotes, perfis dev/test/prod e execução com MySQL](docs/estrutura-e-perfis.md).
- [CI, Checkstyle e testes](docs/ci-testes.md).
- [Autenticação](docs/autenticacao.md).
- [RabbitMQ](docs/rabbitmq.md).

Na raiz, configure as variáveis do `docker-compose.yml` no `.env`: `DB_ROOT_PASSWORD`,
`DB_NAME`, `DB_USER`, `DB_PASSWORD`, `RABBITMQ_USER` e `RABBITMQ_PASSWORD`.
O Compose lê esse arquivo; o processo Java precisa receber suas variáveis pelo ambiente.
Não versione credenciais. `DB_USERNAME` é um alias com precedência sobre `DB_USER`.

```powershell
docker compose up -d
cd application
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

O perfil padrão é `dev`. `JWT_SECRET` é obrigatória também em desenvolvimento,
sem chave padrão versionada. Exporte as mesmas credenciais do banco antes de executar;
veja os exemplos completos no guia de estrutura. Para produção, use
`SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`/`DB_USER`, `DB_PASSWORD` e
`JWT_SECRET` (Base64 de uma chave de ao menos 32 bytes).

As migrations ficam em `application/src/main/resources/db/migration` e usam
`V<versao>__<descricao>.sql`. Nunca altere scripts já aplicados: crie uma nova versão.
O Flyway executa antes do Hibernate, que usa `ddl-auto=validate`;
`open-in-view=false` mantém acesso ao banco dentro das transações.

As migrations criam os cargos `Gerente` e `Garçom`, as permissões iniciais de
funcionários, cargos, produtos, estoque, pedidos e relatórios e o administrador
`gerente@restaurante.com`. A senha inicial é `admin123`, armazenada em BCrypt;
use-a apenas em desenvolvimento e altere-a em ambientes reais. A migration
converte a senha em texto plano do seed legado sem substituir senhas já alteradas.
O inicializador mantém compatibilidade com instalações antigas. Use uma chave
JWT própria para cada ambiente.

```powershell
# Testes isolados, sem serviços externos:
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
# Acrescenta testes em MySQL exclusivo (configuração no guia):
.\mvnw.cmd --batch-mode --no-transfer-progress -Pmysql-integration clean verify
```
