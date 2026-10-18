package com.app.template._1.restaurante.srv.application.config;

import java.util.regex.Pattern;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;
import com.app.template._1.restaurante.srv.application.infra.repository.FuncionarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Pattern BCRYPT_HASH =
            Pattern.compile("\\A\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}\\z");

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(FuncionarioRepository funcionarioRepository, PasswordEncoder passwordEncoder) {
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Funcionario gerente = funcionarioRepository.findByLogin("gerente@restaurante.com");
        if (gerente == null) {
            return;
        }

        String senha = gerente.getSenha();
        if (senha != null && BCRYPT_HASH.matcher(senha).matches()) {
            return;
        }

        gerente.setSenha(passwordEncoder.encode("admin123"));
        funcionarioRepository.save(gerente);
    }
}
