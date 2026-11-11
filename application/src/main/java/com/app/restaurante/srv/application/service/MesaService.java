package com.app.restaurante.srv.application.service;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.MesaFilter;
import com.app.restaurante.srv.application.dto.MesaResponse;
import com.app.restaurante.srv.application.infra.repository.MesaRepository;
import com.app.restaurante.srv.application.infra.specification.MesaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta e listagem paginada de mesas.
 */
@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<MesaResponse> listar(MesaFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Page<MesaResponse> page = mesaRepository.findAll(MesaSpecification.from(filter), pageable)
                .map(MesaResponse::from);
        return PageResponse.from(page);
    }
}
