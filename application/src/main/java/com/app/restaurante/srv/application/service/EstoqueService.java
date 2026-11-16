package com.app.restaurante.srv.application.service;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.EstoqueFilter;
import com.app.restaurante.srv.application.dto.EstoqueResponse;
import com.app.restaurante.srv.application.infra.repository.EstoqueRepository;
import com.app.restaurante.srv.application.infra.specification.EstoqueSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta e listagem paginada de estoque.
 */
@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    public EstoqueService(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EstoqueResponse> listar(EstoqueFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Page<EstoqueResponse> page = estoqueRepository.findAll(EstoqueSpecification.from(filter), pageable)
                .map(EstoqueResponse::from);
        return PageResponse.from(page);
    }
}
