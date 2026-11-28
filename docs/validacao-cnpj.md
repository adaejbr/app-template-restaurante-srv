# REST-03 — Validação de CNPJ (RN001)

`@CNPJ` em `com.ada.erp.shared.validation` verifica formato e os dois dígitos
verificadores (módulo 11). Aceita os formatos sem máscara ou com máscara exata,
numéricos ou alfanuméricos, e letras minúsculas. Rejeita sequências repetidas,
comprimentos errados, caracteres extras, máscara parcial e dígitos incorretos.
`null` é tratado por `@NotBlank`, aplicado no DTO e na entidade.

O cálculo segue o [manual oficial da Receita Federal](https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/documentos-tecnicos/cnpj/manual-dv-cnpj.pdf),
incluindo ASCII menos 48 para letras. Exemplo oficial: `12.ABC.345/01DE-35`.
Esta validação é sintática/matemática; não consulta situação cadastral na Receita.

## Atualização

Como develop ainda não possui o CRUD de restaurante, esta entrega fornece o recorte
`PATCH /api/restaurantes/{id}/cnpj`, autenticado pela configuração JWT existente:

```http
PATCH /api/restaurantes/1/cnpj
Authorization: Bearer <token>
Content-Type: application/json

{"cnpj":"11.222.333/0001-81"}
```

Retorna `200 {"id":1,"cnpj":"11222333000181"}`. CNPJ inválido retorna 400,
duplicado em outro restaurante retorna 409, restaurante inexistente retorna 404
e chamada sem autenticação retorna 401. Outros campos do restaurante não mudam.

`RestauranteService.update` também valida chamadas fora de HTTP, normaliza antes
de consultar duplicidade, exclui o próprio ID e reconhece valores legados com máscara.
Executa saveAndFlush dentro da transação. Somente a violação da constraint
`uq_restaurante_cnpj` é traduzida para 409, incluindo concorrência entre gravações;
falhas de outras constraints não são apresentadas como CNPJ duplicado.
Os callbacks JPA normalizam gravações de outros fluxos; Bean Validation protege
a entidade. SQL direto é reservado a migrations/administração e ignora callbacks.

## Banco e compatibilidade

V1/V2 permanecem imutáveis. V3 cria `usuarios` (entidade preexistente sem migration)
para permitir validate sobre banco vazio. V4 remove máscaras e converte CNPJs
legados para maiúsculas; a constraint UNIQUE existente continua sendo a proteção
contra concorrência. Se dados existentes colidirem após normalização, corrija-os
antes da implantação: a migration falha sem escolher ou apagar restaurantes.
O CNPJ fictício `00000000000000` do seed legado não foi substituído por um número
real de terceiros; sua próxima alteração precisa informar CNPJ válido.

O novo módulo segue `com.ada.erp.restaurante` e shared.validation. A ponte
ErpModulesConfig mantém as entidades/repositórios existentes sem duplicá-los e
é idêntica à entrega INFRA-01. Os testes legados foram alinhados ao pacote real
da Application após a mudança anterior em develop, permitindo executar a suíte.

## Verificação

Com JDK 25, dentro de application:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
# MySQL 8 vazio e exclusivo, porta 3307; credenciais locais de teste:
$env:MYSQL_TEST_URL = 'jdbc:mysql://localhost:3307/infra_test'
$env:MYSQL_TEST_USER = 'infra_test'
$env:MYSQL_TEST_PASSWORD = 'infra_test'
.\mvnw.cmd --batch-mode --no-transfer-progress -Pmysql-integration clean verify
```

A suíte inclui validator, service, HTTP autenticado e persistência H2.
O perfil Maven mysql-integration acrescenta testes com migrations e constraints
reais do MySQL; o workflow Integração MySQL cria seu próprio banco vazio.
