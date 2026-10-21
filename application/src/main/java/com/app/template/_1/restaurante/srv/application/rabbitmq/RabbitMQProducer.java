package com.app.template._1.restaurante.srv.application.rabbitmq;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.app.template._1.restaurante.srv.application.config.RabbitMQConfig;

@Service
public class RabbitMQProducer {

    private static final String TEST_MESSAGE = "Mensagem de teste do RabbitMQ";
    private static final long CONFIRM_TIMEOUT_SECONDS = 5;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTestMessage() {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TEST_EXCHANGE,
                RabbitMQConfig.TEST_ROUTING_KEY,
                TEST_MESSAGE,
                correlationData);

        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new AmqpException("RabbitMQ não confirmou a publicação: " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new AmqpException("RabbitMQ não roteou a mensagem para uma fila.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AmqpException("A confirmação da publicação no RabbitMQ foi interrompida.", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new AmqpException("Não foi possível confirmar a publicação no RabbitMQ.", exception);
        }
    }
}
