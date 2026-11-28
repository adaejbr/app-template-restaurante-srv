package com.ada.erp.cargo.mapper;

import com.ada.erp.cargo.dto.CargoCreateRequest;
import com.ada.erp.cargo.dto.CargoResponse;
import com.ada.erp.usuario.mapper.PermissaoMapper;
import com.app.restaurante.srv.application.core.entity.Cargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Conversao entre a entidade Cargo e os contratos do CRUD (CARGO-02, #66). */
@Mapper(componentModel = "spring", uses = PermissaoMapper.class)
public interface CargoMapper {

    @Mapping(target = "idSetor", source = "setor.id")
    @Mapping(target = "ativo", expression = "java(\"ATIVO\".equalsIgnoreCase(cargo.getStatus()))")
    CargoResponse toResponse(Cargo cargo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "setor", ignore = true)
    @Mapping(target = "status", constant = "ATIVO")
    @Mapping(target = "permissaoCargos", ignore = true)
    Cargo toEntity(CargoCreateRequest request);
}

