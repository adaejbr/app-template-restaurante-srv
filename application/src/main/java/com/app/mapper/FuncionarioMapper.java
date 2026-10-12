package com.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.app.dto.LoginResponse;
import com.app.entity.Funcionario;

@Mapper(componentModel = "spring")
public interface FuncionarioMapper {

    FuncionarioMapper INSTANCE = Mappers.getMapper(FuncionarioMapper.class);

    @Mapping(source = "id", target = "funcionarioId")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "cargo", target = "cargo")
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "token", ignore = true)
    LoginResponse toLoginResponse(Funcionario funcionario);
}