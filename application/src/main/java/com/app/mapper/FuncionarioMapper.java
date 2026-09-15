package com.app.mapper;

import com.app.dto.LoginResponse;
import com.app.entity.Funcionario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FuncionarioMapper {

    FuncionarioMapper INSTANCE = Mappers.getMapper(FuncionarioMapper.class);

    @Mapping(source = "id", target = "funcionarioId")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "cargo", target = "cargo")
    LoginResponse toLoginResponse(Funcionario funcionario);
}