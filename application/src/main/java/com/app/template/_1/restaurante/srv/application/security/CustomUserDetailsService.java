package com.app.template._1.restaurante.srv.application.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.template._1.restaurante.srv.application.core.entity.Funcionario;
import com.app.template._1.restaurante.srv.application.infra.repository.FuncionarioRepository;

/**
 * Carrega o usuário para autenticação buscando o Funcionario pelo login
 * (o campo `login` da tabela guarda o e-mail do funcionário — task j-05).
 *
 * NOTA: implementação mínima criada pela j-05; a j-04 deve revisar/expandir.
 */
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
			throw new UsernameNotFoundException("Funcionário não encontrado: " + email);
		}
		return new CustomUserDetails(funcionario);
	}
}
