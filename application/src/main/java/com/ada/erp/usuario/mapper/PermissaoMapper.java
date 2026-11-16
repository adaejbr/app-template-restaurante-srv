package com.ada.erp.usuario.mapper;

import com.ada.erp.usuario.dto.PermissaoCreateRequest;
import com.ada.erp.usuario.dto.PermissaoResponse;
import com.app.restaurante.srv.application.core.entity.Permissao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Conversao entre a entidade e os contratos do CRUD de permissao. */
@Mapper(componentModel = "spring")
public interface PermissaoMapper {

    PermissaoResponse toResponse(Permissao permissao);

    @Mapping(target = "id", ignore = true)
    Permissao toEntity(PermissaoCreateRequest request);
}
