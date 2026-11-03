package com.app.restaurante.srv.application.dto;

import java.math.BigDecimal;
import com.app.restaurante.srv.application.core.entity.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de saída representando os dados de um produto na listagem.
 *
 * @param id           identificador único do produto
 * @param nome         nome do produto
 * @param valorProduto preço unitário do produto
 */
@Schema(description = "Dados de retorno de um produto.")
public record ProdutoResponse(
        @Schema(description = "ID do produto", example = "1")
        Integer id,

        @Schema(description = "Nome do produto", example = "Pizza Margherita")
        String nome,

        @Schema(description = "Valor unitário do produto", example = "45.90")
        BigDecimal valorProduto) {

    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getValorProduto());
    }
}
