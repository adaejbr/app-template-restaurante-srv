package com.app.restaurante.srv.application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.restaurante.srv.application.dto.MeuPerfilResponse;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.security.CustomUserDetails;
import com.app.restaurante.srv.application.core.entity.Funcionario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/usuarios/me")
public class MeuPerfilController {

    private final CargoFuncionarioRepository cargoFuncionarioRepository;

    public MeuPerfilController(CargoFuncionarioRepository cargoFuncionarioRepository) {
        this.cargoFuncionarioRepository = cargoFuncionarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MEU_PERFIL_VIEW')")
    @Operation(summary = "Exibe os dados e permissões do usuário autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados do perfil e permissões do usuário autenticado",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = MeuPerfilResponse.class))),
        @ApiResponse(responseCode = "401", description = "Bearer token ausente ou inválido", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar o perfil", content = @Content)
    })
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
