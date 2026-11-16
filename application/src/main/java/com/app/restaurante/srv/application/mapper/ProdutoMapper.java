package com.app.restaurante.srv.application.mapper;

import com.app.restaurante.srv.application.core.entity.Produto;
import com.app.restaurante.srv.application.dto.ProdutoResponse;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct para conversão entre {@link Produto} e {@link ProdutoResponse}.
 */
@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    ProdutoResponse toResponse(Produto produto);
}
