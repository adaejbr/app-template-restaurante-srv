package com.app.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.datasource.url=${MYSQL_TEST_URL:jdbc:mysql://localhost:3307/infra_test}",
    "spring.datasource.username=${MYSQL_TEST_USER:infra_test}",
    "spring.datasource.password=${MYSQL_TEST_PASSWORD:infra_test}",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "jwt.secret=YXBsaWNhY2FvLXRlc3RlLWNoYXZlLWp3dC0zMi1ieXRlcw=="
})
@ActiveProfiles("dev")
class InfrastructureMySqlIT {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Environment environment;

    @Test
    void emptyMySqlSchemaIsMigratedAndHibernateValidatesAllEntities() {
        assertTrue(flyway.validateWithResult().validationSuccessful);
        assertEquals(0, flyway.info().pending().length);
        assertTrue(flyway.info().applied().length >= 3);
        assertEquals("validate", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertFalse(environment.getProperty("spring.jpa.open-in-view", Boolean.class, true));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM restaurante", Integer.class));
        assertTrue(jdbc.queryForObject("SELECT senha FROM funcionario WHERE login = ?",
                String.class, "gerente@restaurante.com").startsWith("$2a$"));
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }
}
