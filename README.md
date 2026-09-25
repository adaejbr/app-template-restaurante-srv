# App_Template_01_Restaurante_Backend
Migração de Banco de Dados (Flyway)

Este projeto usa o Flyway para versionar e aplicar automaticamente as alterações no schema do banco MySQL.

Pré-requisitos
Docker e Docker Compose rodando os containers mysql_db, rabbitmq e adminer (ver docker-compose.yml na raiz do projeto)
Arquivo .env na raiz do projeto com as variáveis:
env
  DB_ROOT_PASSWORD=<senha_root>
  DB_NAME=<nome_do_banco>
  DB_USER=<usuario>
  DB_PASSWORD=<senha>
  RABBITMQ_USER=<usuario>
  RABBITMQ_PASSWORD=<senha>
Dependências no pom.xml

⚠️ Importante: a partir do Spring Boot 4.0, o Flyway foi modularizado. Não basta adicionar org.flywaydb:flyway-core sozinho — é necessário o starter oficial do Spring Boot, senão a auto-configuração do Flyway não é ativada (a aplicação sobe normalmente, sem erro, mas nenhuma migration roda).

xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
Configuração (application.properties)
properties
spring.application.name=application

# Datasource
spring.datasource.url=jdbc:mysql://localhost:3306/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
# ddl-auto=validate: o Hibernate NAO cria/altera tabelas, so confere se bate
# com o que o Flyway ja criou. Quem manda no schema e o Flyway.
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

localhost é usado porque a aplicação roda fora do Docker. Se a aplicação também rodar dentro do Docker Compose, trocar por mysql_db (nome do container).

Estrutura das migrations

Os scripts ficam em:

src/main/resources/db/migration/
├── V1__init.sql        -- schema completo (DER)
└── V2__seed_data.sql   -- carga inicial (seed)

Convenção de nome obrigatória: V{versão}__{descrição}.sql

V maiúsculo
Dois underscores entre a versão e a descrição (V1__init, não V1_init) — o Flyway ignora silenciosamente arquivos fora desse padrão
Como rodar

1. Suba os containers (na raiz do projeto):

powershell
docker-compose up -d

2. Rode a aplicação (na pasta application), com as variáveis de ambiente do .env:

powershell
${env:DB_ROOT_PASSWORD}='<senha_root>'; ${env:DB_NAME}='<nome_do_banco>'; ${env:DB_USER}='<usuario>'; ${env:DB_PASSWORD}='<senha>'; ${env:RABBITMQ_USER}='<usuario>'; ${env:RABBITMQ_PASSWORD}='<senha>'; .\mvnw.cmd spring-boot:run

O Flyway roda automaticamente no startup, antes do Hibernate/JPA. No log, deve aparecer:

Successfully validated 2 migrations
Creating Schema History table `<banco>`.`flyway_schema_history` ...
Migrating schema `<banco>` to version "1 - init"
Migrating schema `<banco>` to version "2 - seed data"
Successfully applied 2 migrations to schema `<banco>`, now at version v2
Verificando

Confirmar o histórico de migrations aplicadas:

powershell
docker exec -it mysql_db mysql -u root -p"<senha_root>" -e "USE <nome_do_banco>; SELECT * FROM flyway_schema_history;"

Confirmar os dados do seed:

powershell
docker exec -it mysql_db mysql -u root -p"<senha_root>" -e "USE <nome_do_banco>; SELECT login, status FROM funcionario;"
Adicionando uma nova migration

Nunca edite um script já aplicado (V1, V2, etc.) — o Flyway calcula um checksum de cada arquivo e falha se detectar alteração em uma migration já executada. Para mudar o schema, crie um novo arquivo com o próximo número de versão:

src/main/resources/db/migration/V3__descricao_da_mudanca.sql

Na próxima subida da aplicação, o Flyway detecta e aplica automaticamente só o que for novo.

Resetando o banco em desenvolvimento
powershell
docker exec -it mysql_db mysql -u root -p"<senha_root>" -e "DROP DATABASE <nome_do_banco>; CREATE DATABASE <nome_do_banco>;"

Na próxima subida da aplicação, o Flyway recria tudo do zero (V1 → V2 → ...).

Sobre a senha do funcionário seed (gerente@restaurante.com)

A senha inserida pelo V2__seed_data.sql está em texto plano (admin123). A criptografia (BCrypt) deve ser aplicada pelo backend — via CommandLineRunner no startup ou no momento do login — antes de considerar esse fluxo pronto para produção.