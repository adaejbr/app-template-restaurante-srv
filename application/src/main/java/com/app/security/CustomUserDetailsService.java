package com.app.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // TODO: descomentar e ajustar de acordo com o nome do repositorio
    //private final FuncionarioRepository funcionarioRepository;

    /*
    public CustomUserDetailsService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }
    */
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // TODO: implementar
        return null; 
    }
}