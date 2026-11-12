package com.app.restaurante.srv.application.service;

import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.PedidoFilter;
import com.app.restaurante.srv.application.dto.PedidoResponse;
import com.app.restaurante.srv.application.infra.repository.PedidoRepository;
import com.app.restaurante.srv.application.infra.specification.PedidoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta e listagem paginada de pedidos.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PedidoResponse> listar(PedidoFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Page<PedidoResponse> page = pedidoRepository.findAll(PedidoSpecification.from(filter), pageable)
                .map(PedidoResponse::from);
        return PageResponse.from(page);
    }
}
