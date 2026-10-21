package com.app.template._1.restaurante.srv.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.app.restaurante.srv.application.config.DataInitializer;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:j11;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.flyway.enabled=false",
    "spring.jpa.defer-datasource-initialization=true",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=classpath:db/migration/V2__seed_data.sql"
})
@AutoConfigureMockMvc
@Transactional
class DataInitializerTests {

    private static final String LOGIN = "gerente@restaurante.com";

    @Autowired
    private DataInitializer initializer;

    @Autowired
    private FuncionarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mvc;

    @Test
    void startupHashesSeedInDatabaseAndAllowsLogin() throws Exception {
        String hash = savedPassword();
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(passwordEncoder.matches("admin123", hash));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gerente@restaurante.com\",\"senha\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.cargo").value("GERENTE"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gerente@restaurante.com\",\"senha\":\"incorreta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedInitializationPreservesStoredHash() {
        String original = savedPassword();
        initializer.run();
        initializer.run();
        repository.flush();
        assertEquals(original, savedPassword());
    }

    @ParameterizedTest
    @EnumSource(BCryptVersion.class)
    void initializationPreservesChangedPasswordsAndBcryptVersions(BCryptVersion version) {
        Funcionario gerente = repository.findByLogin(LOGIN);
        String changed = new BCryptPasswordEncoder(version).encode("senha-alterada");
        gerente.setSenha(changed);
        repository.saveAndFlush(gerente);

        initializer.run();
        repository.flush();

        assertEquals(changed, savedPassword());
        assertTrue(passwordEncoder.matches("senha-alterada", savedPassword()));
    }

    @Test
    void missingSeedDoesNotChangeAnotherEmployee() {
        Funcionario gerente = repository.findByLogin(LOGIN);
        gerente.setLogin("outro@restaurante.com");
        gerente.setSenha("outra-senha");
        repository.saveAndFlush(gerente);

        initializer.run();
        repository.flush();

        assertEquals("outra-senha", jdbc.queryForObject(
                "SELECT senha FROM funcionario WHERE login = ?", String.class, "outro@restaurante.com"));
        assertEquals(1L, repository.count());
    }

    private String savedPassword() {
        return jdbc.queryForObject("SELECT senha FROM funcionario WHERE login = ?", String.class, LOGIN);
    }
}
