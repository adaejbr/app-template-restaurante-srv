package com.app.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.flywaydb.core.Flyway;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ActiveProfiles("test")
@SpringBootTest(properties = {
    "spring.datasource.url=${MYSQL_TEST_URL:jdbc:mysql://localhost:3307/infra_test}",
    "spring.datasource.username=${MYSQL_TEST_USER:infra_test}",
    "spring.datasource.password=${MYSQL_TEST_PASSWORD:infra_test}",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration"
})
class RestauranteCnpjMySqlIT extends RestauranteCnpjTests {
    @Autowired
    private Flyway flyway;

    @Test
    void allMigrationsAreAppliedAndValidated() {
        assertTrue(flyway.validateWithResult().validationSuccessful);
        assertTrue(flyway.info().applied().length >= 4);
        assertEquals(0, flyway.info().pending().length);
    }
}
