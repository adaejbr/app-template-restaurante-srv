package com.app.template._1.restaurante.srv.application;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.app.template._1.restaurante.srv.application.core.entity.Cargo;
import com.app.template._1.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;
import com.app.template._1.restaurante.srv.application.core.entity.Perfil;
import com.app.template._1.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.template._1.restaurante.srv.application.infra.repository.FuncionarioRepository;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthTests {

    private static final String EMAIL = "gerente@restaurante.com";
    private static final String PASSWORD = "admin123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private FuncionarioRepository funcionarioRepository;

    @MockitoBean
    private CargoFuncionarioRepository cargoFuncionarioRepository;

    @BeforeEach
    void prepareUsers() {
        Funcionario active = funcionario(EMAIL, PASSWORD, "ATIVO");
        when(funcionarioRepository.findByLogin(EMAIL)).thenReturn(active);
        when(funcionarioRepository.findByLogin("inativo@restaurante.com"))
                .thenReturn(funcionario("inativo@restaurante.com", PASSWORD, "INATIVO"));
        when(cargoFuncionarioRepository.findFirstByIdFuncionarioAndStatus(1, "ATIVO"))
                .thenReturn(Optional.of(CargoFuncionario.builder()
                        .idFuncionario(1)
                        .idCargo(1)
                        .status("ATIVO")
                        .cargo(Cargo.builder().nome("GERENTE").build())
                        .build()));
    }

    @Test
    void validCredentialsReturnJwtAndEmployeeData() throws Exception {
        String response = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                Map.of("email", EMAIL, "senha", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.nome").value("Gerente de Teste"))
                .andExpect(jsonPath("$.cargo").value("GERENTE"))
                .andExpect(jsonPath("$.username").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = mapper.readTree(response).get("token").asString();
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(EMAIL))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
    }

    @Test
    void invalidCredentialsReturnUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                Map.of("email", EMAIL, "senha", "senha-incorreta"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveEmployeeCannotLogin() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "inativo@restaurante.com", "senha", PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRequiresEmailAndPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpointRejectsMissingOrInvalidBearerToken() throws Exception {
        mvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    private Funcionario funcionario(String login, String senha, String status) {
        return Funcionario.builder()
                .id(1)
                .perfil(Perfil.builder().nome("Gerente de Teste").build())
                .login(login)
                .senha(senha)
                .status(status)
                .build();
    }
}
