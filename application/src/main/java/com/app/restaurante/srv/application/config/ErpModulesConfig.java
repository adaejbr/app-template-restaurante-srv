package com.app.restaurante.srv.application.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Registers ERP modules alongside the existing application during gradual migration. */
@Configuration
@ComponentScan("com.ada.erp")
@EntityScan(basePackages = {"com.app.restaurante.srv.application", "com.ada.erp"})
@EnableJpaRepositories(basePackages = {"com.app.restaurante.srv.application", "com.ada.erp"})
public class ErpModulesConfig {
}
