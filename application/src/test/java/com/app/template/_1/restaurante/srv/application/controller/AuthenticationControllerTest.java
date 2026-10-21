package com.app.template._1.restaurante.srv.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;
import com.app.template._1.restaurante.srv.application.core.entity.Perfil;
import com.app.template._1.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.template._1.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.template._1.restaurante.srv.application.security.CustomUserDetailsService;
import com.app.template._1.restaurante.srv.application.security.JwtUtils;
import com.app.template._1.restaurante.srv.application.security.SecurityConfig;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class})
class AuthenticationControllerTest {

    private static final String EMAIL = "gerente@restaurante.com";
    private static final String PASSWORD = "senha-teste";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private FuncionarioRepository funcionarioRepository;

    @MockitoBean
    private CargoFuncionarioRepository cargoFuncionarioRepository;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    void validCredentialsReturnEmployeeDataWithoutActiveRole() throws Exception {
        Funcionario funcionario = Funcionario.builder()
                .id(1)
                .perfil(Perfil.builder().nome("Gerente de Teste").build())
                .login(EMAIL)
                .senha(passwordEncoder.encode(PASSWORD))
                .status("ATIVO")
                .build();
        when(funcionarioRepository.findByLogin(EMAIL)).thenReturn(funcionario);
        when(cargoFuncionarioRepository.findFirstByIdFuncionarioAndStatus(1, "ATIVO"))
                .thenReturn(Optional.empty());
        when(jwtUtils.generateToken(any())).thenReturn("jwt-de-teste");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"gerente@restaurante.com","senha":"senha-teste"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-de-teste"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.nome").value("Gerente de Teste"))
                .andExpect(jsonPath("$.cargo").hasJsonPath())
                .andExpect(jsonPath("$.cargo").value((Object) null))
                .andExpect(jsonPath("$.senha").doesNotExist());

        verify(funcionarioRepository).findByLogin(EMAIL);
        verify(cargoFuncionarioRepository).findFirstByIdFuncionarioAndStatus(1, "ATIVO");
    }

    @Test
    void unknownEmployeeReturnsUnauthorized() throws Exception {
        when(funcionarioRepository.findByLogin("inexistente@restaurante.com")).thenReturn(null);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"inexistente@restaurante.com","senha":"senha-teste"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"));

        verify(funcionarioRepository).findByLogin("inexistente@restaurante.com");
        verifyNoInteractions(cargoFuncionarioRepository, jwtUtils);
    }

    @Test
    void invalidEmailReturnsBadRequestBeforeAuthentication() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"email-invalido","senha":"senha-teste"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(funcionarioRepository, cargoFuncionarioRepository, jwtUtils);
    }
}
