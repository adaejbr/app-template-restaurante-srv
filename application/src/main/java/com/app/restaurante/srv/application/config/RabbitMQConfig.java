package com.app.restaurante.srv.application.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TEST_QUEUE = "restaurante.test.queue";
    public static final String TEST_EXCHANGE = "restaurante.test.exchange";
    public static final String TEST_ROUTING_KEY = "restaurante.test.routing-key";

    @Bean
    Queue testQueue() {
        return QueueBuilder.durable(TEST_QUEUE).build();
    }

    @Bean
    DirectExchange testExchange() {
        return new DirectExchange(TEST_EXCHANGE);
    }

    @Bean
    Binding testBinding(Queue testQueue, DirectExchange testExchange) {
        return BindingBuilder.bind(testQueue).to(testExchange).with(TEST_ROUTING_KEY);
    }
}
