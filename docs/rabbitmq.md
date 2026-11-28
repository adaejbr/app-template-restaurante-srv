# Integração de teste com RabbitMQ

O `docker-compose.yml` já configura o RabbitMQ com o plugin de gerenciamento, AMQP em `5672` e a interface web em `15672`. As credenciais vêm de `RABBITMQ_USER` e `RABBITMQ_PASSWORD` no `.env` da raiz.

## Subir e verificar o broker

Na raiz do repositório, execute:

```powershell
docker-compose up -d rabbitmq
docker-compose ps rabbitmq
docker inspect --format '{{.State.Health.Status}}' rabbitmq
docker-compose logs --tail=50 rabbitmq
```

O container `rabbitmq` deve aparecer em execução e o comando `docker inspect` deve retornar `healthy`; os logs devem indicar que o broker iniciou. A aplicação local conecta em `localhost:5672`; `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER` e `RABBITMQ_PASSWORD` podem ser definidos no ambiente para sobrescrever os padrões locais.

Abra [http://localhost:15672](http://localhost:15672) e entre com os valores de `RABBITMQ_USER` e `RABBITMQ_PASSWORD` definidos no `.env`. Se necessário, confirme as portas publicadas com `docker-compose port rabbitmq 5672` e `docker-compose port rabbitmq 15672`.

## Executar e chamar o endpoint

Suba também os serviços necessários à aplicação e configure as variáveis já documentadas no README. Com a aplicação disponível em `http://localhost:8080`, autentique-se e publique a mensagem:

```powershell
$login = Invoke-RestMethod `
  -Uri 'http://localhost:8080/api/auth/login' `
  -Method Post `
  -ContentType 'application/json' `
  -Body '{"email":"gerente@restaurante.com","senha":"admin123"}'

Invoke-RestMethod `
  -Uri 'http://localhost:8080/api/teste/rabbit' `
  -Method Post `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

O endpoint exige JWT e envia uma mensagem estática. Uma publicação só retorna sucesso depois de confirmação do RabbitMQ e verificação de que a mensagem foi roteada. Uma falha de conexão, confirmação negativa ou mensagem não roteada resulta em erro HTTP, não em resposta de sucesso.

## Conferir a mensagem

No Management UI:

1. Abra **Queues and Streams** e selecione `restaurante.test.queue`.
2. Confira **Ready messages** (deve haver pelo menos uma mensagem após a chamada).
3. Em **Get messages**, busque uma mensagem; selecione requeue para mantê-la na fila durante a inspeção.

A fila durável `restaurante.test.queue` é ligada à exchange `restaurante.test.exchange` pela routing key `restaurante.test.routing-key`. Cada chamada gera mais uma mensagem, pois não há consumidor configurado neste teste.
