package com.ada.erp.restaurante.mapper;

import com.ada.erp.restaurante.dto.EnderecoDTO;
import com.ada.erp.restaurante.dto.RestauranteResponse;
import com.app.restaurante.srv.application.core.entity.Cidade;
import com.app.restaurante.srv.application.core.entity.Endereco;
import com.app.restaurante.srv.application.core.entity.Estado;
import com.app.restaurante.srv.application.core.entity.Restaurante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Conversao do restaurante para o contrato de leitura, com o endereco embutido. */
@Mapper(componentModel = "spring")
public interface RestauranteMapper {

    @Mapping(target = "endereco", expression = "java(paraEndereco(restaurante.getEndereco()))")
    RestauranteResponse toResponse(Restaurante restaurante);

    /** Achata o endereco, resolvendo cidade e estado que sao lazy na entidade. */
    default EnderecoDTO paraEndereco(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        Cidade cidade = endereco.getCidade();
        Estado estado = cidade == null ? null : cidade.getEstado();
        return new EnderecoDTO(endereco.getLogradouro(), endereco.getNumero(), endereco.getReferencia(),
                cidade == null ? null : cidade.getNome(), estado == null ? null : estado.getNome());
    }
}
