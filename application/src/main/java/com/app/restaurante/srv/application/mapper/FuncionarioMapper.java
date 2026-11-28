package com.app.restaurante.srv.application.mapper;

import com.app.restaurante.srv.application.core.entity.Cargo;
import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.dto.FuncionarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para conversão entre {@link Funcionario} e {@link FuncionarioResponse}.
 */
@Mapper(componentModel = "spring")
public interface FuncionarioMapper {

    String STATUS_ATIVO = "ATIVO";

    @Mapping(target = "nome", source = "perfil.nome")
    @Mapping(target = "cpf", source = "perfil.cpf")
    @Mapping(target = "telefone", source = "perfil.telefone")
    @Mapping(target = "idCargo", expression = "java(extrairIdCargoAtivo(funcionario))")
    @Mapping(target = "nomeCargo", expression = "java(extrairNomeCargoAtivo(funcionario))")
    FuncionarioResponse toResponse(Funcionario funcionario);

    @Mapping(target = "id", source = "funcionario.id")
    @Mapping(target = "login", source = "funcionario.login")
    @Mapping(target = "nome", source = "funcionario.perfil.nome")
    @Mapping(target = "cpf", source = "funcionario.perfil.cpf")
    @Mapping(target = "telefone", source = "funcionario.perfil.telefone")
    @Mapping(target = "status", source = "funcionario.status")
    @Mapping(target = "idCargo", source = "cargo.id")
    @Mapping(target = "nomeCargo", source = "cargo.nome")
    FuncionarioResponse toResponse(Funcionario funcionario, Cargo cargo);

    default Integer extrairIdCargoAtivo(Funcionario funcionario) {
        CargoFuncionario ativo = buscarCargoAtivo(funcionario);
        return ativo != null ? ativo.getIdCargo() : null;
    }

    default String extrairNomeCargoAtivo(Funcionario funcionario) {
        CargoFuncionario ativo = buscarCargoAtivo(funcionario);
        return ativo != null && ativo.getCargo() != null ? ativo.getCargo().getNome() : null;
    }

    private CargoFuncionario buscarCargoAtivo(Funcionario funcionario) {
        if (funcionario == null || funcionario.getCargos() == null) {
            return null;
        }
        return funcionario.getCargos().stream()
                .filter(vinculo -> STATUS_ATIVO.equalsIgnoreCase(vinculo.getStatus()))
                .findFirst()
                .orElse(null);
    }
}
