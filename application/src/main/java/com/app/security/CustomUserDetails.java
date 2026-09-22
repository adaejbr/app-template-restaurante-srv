package com.app.security;

import com.app.entity.Funcionario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Funcionario funcionario;

    public CustomUserDetails(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // mapear os cargos aqui
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        // ajustar getter de acordo com classe funcionario
        return funcionario.getSenha(); 
    }

    @Override
    public String getUsername() {
        // ajustar getter de acordo com classe funcionario
        return funcionario.getEmail(); 
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; 
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }
}