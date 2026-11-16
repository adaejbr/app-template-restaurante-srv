package com.app.restaurante.srv.application.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.app.restaurante.srv.application.core.entity.Funcionario;

public class CustomUserDetails implements UserDetails {

    private final Funcionario funcionario;
    private final String cargo;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Funcionario funcionario, String cargo, Collection<String> permissoes) {
        this.funcionario = funcionario;
        this.cargo = cargo;
        List<GrantedAuthority> carregadas = new ArrayList<>();
        carregadas.add(new SimpleGrantedAuthority("ROLE_USER"));
        for (String permissao : permissoes) {
            carregadas.add(new SimpleGrantedAuthority(permissao));
        }
        this.authorities = List.copyOf(carregadas);
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public String getCargo() {
        return cargo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return funcionario.getSenha();
    }

    @Override
    public String getUsername() {
        return funcionario.getLogin();
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
        return "ATIVO".equals(funcionario.getStatus());
    }
}
