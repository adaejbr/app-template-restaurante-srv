package com.app.template._1.restaurante.srv.application.rabbitmq;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.app.restaurante.srv.application.config.RabbitMQConfig;
import com.app.restaurante.srv.application.rabbitmq.RabbitMQProducer;

class RabbitMQProducerTest {

    @Test
    void publishesMessageAndWaitsForBrokerConfirmation() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(3);
            correlationData.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.TEST_EXCHANGE),
                eq(RabbitMQConfig.TEST_ROUTING_KEY),
                eq("Mensagem de teste do RabbitMQ"),
                any(CorrelationData.class));

        new RabbitMQProducer(rabbitTemplate).publishTestMessage();

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.TEST_EXCHANGE),
                eq(RabbitMQConfig.TEST_ROUTING_KEY),
                eq("Mensagem de teste do RabbitMQ"),
                any(CorrelationData.class));
    }

    @Test
    void rejectsNegativeBrokerConfirmation() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(3);
            correlationData.getFuture().complete(new CorrelationData.Confirm(false, "broker nack"));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.TEST_EXCHANGE),
                eq(RabbitMQConfig.TEST_ROUTING_KEY),
                any(String.class),
                any(CorrelationData.class));

        assertThrows(AmqpException.class, () -> new RabbitMQProducer(rabbitTemplate).publishTestMessage());
    }
}
