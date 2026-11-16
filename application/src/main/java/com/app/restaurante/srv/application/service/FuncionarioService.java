package com.app.restaurante.srv.application.service;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.restaurante.srv.application.infra.specification.FuncionarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta e listagem paginada de funcionários.
 */
@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<FuncionarioListResponse> listar(FuncionarioFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Page<FuncionarioListResponse> page = funcionarioRepository
                .findAll(FuncionarioSpecification.from(filter), pageable)
                .map(FuncionarioListResponse::from);
        return PageResponse.from(page);
    }
}
