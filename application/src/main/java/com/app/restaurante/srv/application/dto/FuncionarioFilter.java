package com.app.restaurante.srv.application.dto;

import com.app.restaurante.srv.application.core.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtro de consulta paginada para Funcionario.
 */
@Getter
@Setter
public class FuncionarioFilter extends BaseFilter {

    @Schema(description = "Status do funcionário (ex: ATIVO, INATIVO)", example = "ATIVO")
    private String status;
}
