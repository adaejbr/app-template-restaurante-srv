package com.ada.erp.restaurante.dto;

/** Endereco do restaurante, achatado para o contrato da API. */
public record EnderecoDTO(
        String logradouro,
        String numero,
        String referencia,
        String cidade,
        String estado) {
}
