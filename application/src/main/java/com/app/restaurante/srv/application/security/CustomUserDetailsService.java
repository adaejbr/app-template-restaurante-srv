package com.app.restaurante.srv.application.security;

import java.util.List;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.PermissaoCargoRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final FuncionarioRepository funcionarioRepository;
    private final CargoFuncionarioRepository cargoFuncionarioRepository;
    private final PermissaoCargoRepository permissaoCargoRepository;

    public CustomUserDetailsService(FuncionarioRepository funcionarioRepository,
            CargoFuncionarioRepository cargoFuncionarioRepository,
            PermissaoCargoRepository permissaoCargoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.cargoFuncionarioRepository = cargoFuncionarioRepository;
        this.permissaoCargoRepository = permissaoCargoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Funcionario funcionario = funcionarioRepository.findByLogin(email);
        if (funcionario == null) {
            throw new UsernameNotFoundException("Funcionário não encontrado");
        }
        Optional<CargoFuncionario> cargoAtivo =
                cargoFuncionarioRepository.findFirstByIdFuncionarioAndStatus(funcionario.getId(), "ATIVO");
        String cargo = cargoAtivo.map(cargoFuncionario -> cargoFuncionario.getCargo().getNome()).orElse(null);
        List<String> permissoes = cargoAtivo
                .map(cargoFuncionario -> permissaoCargoRepository
                        .findChavesAtivasByCargo(cargoFuncionario.getIdCargo()))
                .orElse(List.of());
        return new CustomUserDetails(funcionario, cargo, permissoes);
    }
}
