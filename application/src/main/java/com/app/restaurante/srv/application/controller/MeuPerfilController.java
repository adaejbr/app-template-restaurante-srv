package com.app.restaurante.srv.application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.restaurante.srv.application.dto.MeuPerfilResponse;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.security.CustomUserDetails;
import com.app.restaurante.srv.application.core.entity.Funcionario;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/usuarios/me")
public class MeuPerfilController {

    private final CargoFuncionarioRepository cargoFuncionarioRepository;

    public MeuPerfilController(CargoFuncionarioRepository cargoFuncionarioRepository){
        this.cargoFuncionarioRepository = cargoFuncionarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MEU_PERFIL_VIEW')")
    @Operation(summary = "Exibe os dados e permissões do usuário autenticado")
    public MeuPerfilResponse getMeuPerfil() {  
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Funcionario funcionario = userDetails.getFuncionario();
        Integer funcionarioId = funcionario.getId();

        String cargo = cargoFuncionarioRepository
                .findFirstByIdFuncionarioAndStatus(funcionarioId, "ATIVO")
                .map(cargoFuncionario -> cargoFuncionario.getCargo().getNome())
                .orElse(null);

        List<String> permissoes = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();

        return new MeuPerfilResponse(
                funcionarioId,
                funcionario.getPerfil().getNome(),
                funcionario.getLogin(),
                funcionario.getPerfil().getCpf(),
                funcionario.getPerfil().getTelefone(),
                cargo,
                permissoes);
    }
}
