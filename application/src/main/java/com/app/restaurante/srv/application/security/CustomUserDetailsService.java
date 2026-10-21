package com.app.restaurante.srv.application.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final FuncionarioRepository funcionarioRepository;

    public CustomUserDetailsService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Funcionario funcionario = funcionarioRepository.findByLogin(email);
        if (funcionario == null) {
            throw new UsernameNotFoundException("Funcionário não encontrado");
        }
        return new CustomUserDetails(funcionario);
    }
}
