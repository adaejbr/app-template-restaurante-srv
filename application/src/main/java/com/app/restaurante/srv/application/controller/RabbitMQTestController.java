package com.app.restaurante.srv.application.controller;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.restaurante.srv.application.rabbitmq.RabbitMQProducer;

@RestController
@RequestMapping("/api/teste/rabbit")
public class RabbitMQTestController {

    private final RabbitMQProducer rabbitMQProducer;

    public RabbitMQTestController(RabbitMQProducer rabbitMQProducer) {
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @PostMapping
    @Operation(summary = "Publicar mensagem de teste no RabbitMQ")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mensagem confirmada pelo RabbitMQ"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "500", description = "Falha ao publicar ou confirmar a mensagem")
    })
    public ResponseEntity<Map<String, String>> publishTestMessage() {
        rabbitMQProducer.publishTestMessage();
        return ResponseEntity.ok(Map.of("mensagem", "Mensagem publicada e confirmada pelo RabbitMQ."));
    }
}
