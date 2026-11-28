# FUNC-01 — Modelo de Perfil e Funcionário

O modelo mantém o DER e as migrations existentes. Perfil contém os dados pessoais;
Cargo representa a função e suas permissões. Não são conceitos equivalentes.

| Relação | Mapeamento | Integridade |
| --- | --- | --- |
| Funcionário → Perfil | OneToOne, obrigatório | FK e UNIQUE em funcionario.id_perfil |
| Perfil → Endereço | ManyToOne, obrigatório | FK em perfil.id_endereco |
| CargoFuncionario → Funcionário / Cargo | ManyToOne em cada lado | PK composta e duas FKs |
| PermissaoCargo → Cargo / Permissão | ManyToOne em cada lado | PK composta e duas FKs |

CPF e login têm restrições UNIQUE no banco. Os valores de CPF seguem o formato
já utilizado no projeto; validação e normalização na API pertencem à FUNC-02.
O login continua armazenando o e-mail, e o status permanece String para preservar
a autenticação existente. As senhas continuam sendo hashes BCrypt no fluxo atual.

## Migrations

V1__init.sql já cria perfil, funcionario e as tabelas associativas, com suas
constraints. Não se recriam essas tabelas nem se alteram migrations aplicadas.
V5__perfil_funcionario_indexes.sql acrescenta índices de nome do perfil, status
do funcionário e funcionário/status no vínculo com cargo, sem transformar a
relação N:N em um único cargo. O Hibernate usa os mesmos índices no schema de testes.

## Consulta e persistência

FuncionarioRepository.findDetalhadoById(Integer) carrega perfil, vínculos de cargo,
cargos e suas permissões. A consulta retorna Optional vazio para ID inexistente
e funciona sem Open Session in View. As coleções usam Set e são excluídas de
toString/equals/hashCode para evitar recursão nas associações bidirecionais.
A consulta não filtra vínculos inativos: o consumidor deve aplicar status ATIVO
no cargo_funcionario e situacao ATIVA em permissao_cargo antes de conceder acesso.
Não altera as authorities da autenticação existente.

findByLogin/findByEmail preservam o carregamento do perfil usado pelo login.
As coleções são LAZY nas consultas comuns. Não há cascade de persistência ou remoção
para entidades compartilhadas. Salve as entidades e os vínculos por seus repositórios,
em uma transação. Os campos idCargo e idFuncionario são os donos das colunas na
associação com IdClass; atribuir somente cargo/funcionario não grava as chaves.

## Validação

FuncionarioModeloTests cobre carregamento do perfil e permissões, múltiplos cargos,
ausência de cargo, compatibilidade da consulta de login e rejeição no banco de CPF,
login e vínculo de perfil duplicados, FKs inválidas, vínculo de cargo duplicado e
campos obrigatórios ausentes. Cada teste executa em transação com rollback.

Execute na pasta application:

- mvnw.cmd test: testes H2 e Checkstyle.
- mvnw.cmd -Pmysql-integration verify: os mesmos testes de modelo em MySQL,
  com Flyway habilitado e Hibernate validate, incluindo validação das migrations.

O perfil mysql-integration usa MYSQL_TEST_URL, MYSQL_TEST_USER e MYSQL_TEST_PASSWORD
com os padrões documentados em RestauranteCnpjMySqlIT. O workflow de integração
existente executa automaticamente a nova classe FuncionarioModeloMySqlIT.

A tarefa prepara a estrutura para RBAC e consulta do próprio perfil; endpoints de
cadastro, edição, exclusão e implementação das regras de autorização têm tarefas próprias.
