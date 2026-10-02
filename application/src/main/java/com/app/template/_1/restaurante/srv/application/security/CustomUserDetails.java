package com.app.template._1.restaurante.srv.application.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;

/**
 * Adapter entre a entidade {@link Funcionario} e a interface {@link UserDetails}
 * do Spring Security (task j-05).
 *
 * NOTA: implementação mínima criada pela j-05; a j-04 deve mapear permissões reais.
 */
public class CustomUserDetails implements UserDetails {

	private final Funcionario funcionario;

	public CustomUserDetails(Funcionario funcionario) {
		this.funcionario = funcionario;
	}

	public Funcionario getFuncionario() {
		return funcionario;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO j-04: mapear permissões reais (permissao_cargo) como authorities
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
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
