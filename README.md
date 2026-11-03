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
.\mvnw.cmd spring-boot:run
```

O perfil padrão é `dev`. Exporte as mesmas credenciais do banco antes de executar;
veja os exemplos completos no guia de estrutura. Para produção, use
`SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`/`DB_USER`, `DB_PASSWORD` e
`JWT_SECRET` (Base64 de uma chave de ao menos 32 bytes).

As migrations ficam em `application/src/main/resources/db/migration` e usam
`V<versao>__<descricao>.sql`. Nunca altere scripts já aplicados: crie uma nova versão.
O Flyway executa antes do Hibernate, que usa `ddl-auto=validate`;
`open-in-view=false` mantém acesso ao banco dentro das transações.

O seed legado cria `gerente@restaurante.com`; o inicializador converte a senha
inicial em BCrypt de forma idempotente. A credencial de demonstração e a chave JWT
local são exclusivas do desenvolvimento.

```powershell
# Testes isolados, sem serviços externos:
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
# Acrescenta testes em MySQL exclusivo (configuração no guia):
.\mvnw.cmd --batch-mode --no-transfer-progress -Pmysql-integration clean verify
```
