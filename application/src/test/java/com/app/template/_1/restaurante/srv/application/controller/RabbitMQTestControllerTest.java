package com.app.template._1.restaurante.srv.application.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.app.template._1.restaurante.srv.application.rabbitmq.RabbitMQProducer;
import com.app.template._1.restaurante.srv.application.security.CustomUserDetailsService;
import com.app.template._1.restaurante.srv.application.security.JwtUtils;
import com.app.template._1.restaurante.srv.application.security.SecurityConfig;
import com.app.template._1.restaurante.srv.application.infra.repository.FuncionarioRepository;

@WebMvcTest(RabbitMQTestController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class})
class RabbitMQTestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RabbitMQProducer rabbitMQProducer;

    @MockitoBean
    private FuncionarioRepository funcionarioRepository;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    void publishesMessageForAuthenticatedUser() throws Exception {
        mvc.perform(post("/api/teste/rabbit").with(user("teste")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem")
                        .value("Mensagem publicada e confirmada pelo RabbitMQ."));

        verify(rabbitMQProducer).publishTestMessage();
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(post("/api/teste/rabbit"))
                .andExpect(status().isUnauthorized());
    }
}
