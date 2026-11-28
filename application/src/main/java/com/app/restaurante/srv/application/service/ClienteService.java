package com.app.restaurante.srv.application.service;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.ClienteFilter;
import com.app.restaurante.srv.application.dto.ClienteResponse;
import com.app.restaurante.srv.application.infra.repository.ClienteRepository;
import com.app.restaurante.srv.application.infra.specification.ClienteSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta e listagem paginada de clientes.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> listar(ClienteFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Page<ClienteResponse> page = clienteRepository.findAll(ClienteSpecification.from(filter), pageable)
                .map(ClienteResponse::from);
        return PageResponse.from(page);
    }
}
