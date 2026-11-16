package com.app.restaurante.srv.application.service;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.ComandaFilter;
import com.app.restaurante.srv.application.dto.ComandaResponse;
import com.app.restaurante.srv.application.infra.repository.ComandaRepository;
import com.app.restaurante.srv.application.infra.specification.ComandaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta e listagem paginada de comandas.
 */
@Service
public class ComandaService {

    private final ComandaRepository comandaRepository;

    public ComandaService(ComandaRepository comandaRepository) {
        this.comandaRepository = comandaRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ComandaResponse> listar(ComandaFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Page<ComandaResponse> page = comandaRepository.findAll(ComandaSpecification.from(filter), pageable)
                .map(ComandaResponse::from);
        return PageResponse.from(page);
    }
}
