package com.app.restaurante.srv.application.service;

import com.app.restaurante.srv.application.core.entity.Produto;
import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.ProdutoFilter;
import com.app.restaurante.srv.application.dto.ProdutoResponse;
import com.app.restaurante.srv.application.infra.repository.ProdutoRepository;
import com.app.restaurante.srv.application.infra.specification.ProdutoSpecification;
import com.app.restaurante.srv.application.mapper.ProdutoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pela consulta e listagem paginada de produtos.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    public ProdutoService(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProdutoResponse> listar(ProdutoFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Specification<Produto> specification = ProdutoSpecification.from(filter);
        Page<ProdutoResponse> page = produtoRepository.findAll(specification, pageable)
                .map(produtoMapper::toResponse);
        return PageResponse.from(page);
    }
}
