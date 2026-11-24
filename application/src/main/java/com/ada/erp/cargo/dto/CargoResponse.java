package com.ada.erp.cargo.dto;

import java.util.List;
import com.ada.erp.usuario.dto.PermissaoResponse;

/** Representacao de leitura de um cargo com suas permissoes ativas embutidas. */
public record CargoResponse(
        Integer id,
        String nome,
        Integer idSetor,
        String status,
        boolean ativo,
        List<PermissaoResponse> permissoes) {
}
