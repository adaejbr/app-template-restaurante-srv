# App_Template_01_Restaurante_Backend

Backend Spring Boot 4.1, Java 25 e MySQL 8, com autentica??o JWT e schema gerenciado por Flyway.

- [Estrutura de pacotes, perfis dev/test/prod e execu??o com MySQL](docs/estrutura-e-perfis.md).
- [CI, Checkstyle e testes](docs/ci-testes.md).
- [Autentica??o](docs/autenticacao.md).
- [RabbitMQ](docs/rabbitmq.md).

Na raiz, configure as vari?veis do `docker-compose.yml` no `.env`: `DB_ROOT_PASSWORD`,
`DB_NAME`, `DB_USER`, `DB_PASSWORD`, `RABBITMQ_USER` e `RABBITMQ_PASSWORD`.
O Compose l? esse arquivo; o processo Java precisa receber suas vari?veis pelo ambiente.
N?o versione credenciais. `DB_USERNAME` ? um alias com preced?ncia sobre `DB_USER`.

```powershell
docker compose up -d
cd application
.\mvnw.cmd spring-boot:run
```

O perfil padr?o ? `dev`. Exporte as mesmas credenciais do banco antes de executar;
veja os exemplos completos no guia de estrutura. Para produ??o, use
`SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`/`DB_USER`, `DB_PASSWORD` e
`JWT_SECRET` (Base64 de uma chave de ao menos 32 bytes).

As migrations ficam em `application/src/main/resources/db/migration` e usam
`V<versao>__<descricao>.sql`. Nunca altere scripts j? aplicados: crie uma nova vers?o.
O Flyway executa antes do Hibernate, que usa `ddl-auto=validate`;
`open-in-view=false` mant?m acesso ao banco dentro das transa??es.

O seed legado cria `gerente@restaurante.com`; o inicializador converte a senha
inicial em BCrypt de forma idempotente. A credencial de demonstra??o e a chave JWT
local s?o exclusivas do desenvolvimento.

```powershell
# Testes isolados, sem servi?os externos:
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
# Acrescenta testes em MySQL exclusivo (configura??o no guia):
.\mvnw.cmd --batch-mode --no-transfer-progress -Pmysql-integration clean verify
```
