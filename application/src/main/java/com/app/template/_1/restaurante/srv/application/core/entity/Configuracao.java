package com.app.template._1.restaurante.srv.application.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "configuracao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "taxa_servico", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaServico = BigDecimal.ZERO;

    @Column(name = "horario_abertura")
    private LocalTime horarioAbertura;

    @Column(name = "horario_fechamento")
    private LocalTime horarioFechamento;
}