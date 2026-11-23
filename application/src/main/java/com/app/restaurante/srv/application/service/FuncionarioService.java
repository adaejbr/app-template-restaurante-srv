package com.app.restaurante.srv.application.service;

import java.util.List;
import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.restaurante.srv.application.infra.specification.FuncionarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta e listagem paginada de funcionários.
 */
@Service
public class FuncionarioService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "perfil.nome");

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<FuncionarioListResponse> listar(FuncionarioFilter filter) {
        Pageable pageable = resolvePageable(filter);
        Page<FuncionarioListResponse> page = funcionarioRepository
                .findAll(FuncionarioSpecification.from(filter), pageable)
                .map(FuncionarioListResponse::from);
        return PageResponse.from(page);
    }

    private Pageable resolvePageable(FuncionarioFilter filter) {
        Pageable base = PageableFactory.from(filter);
        Sort sort = base.getSort();
        if (sort.isUnsorted()) {
            return PageRequest.of(base.getPageNumber(), base.getPageSize(), DEFAULT_SORT);
        }
        List<Sort.Order> mappedOrders = sort.stream()
                .map(order -> order.withProperty(mapSortProperty(order.getProperty())))
                .toList();
        return PageRequest.of(base.getPageNumber(), base.getPageSize(), Sort.by(mappedOrders));
    }

    private String mapSortProperty(String property) {
        return switch (property) {
            case "nome" -> "perfil.nome";
            case "cpf" -> "perfil.cpf";
            case "email" -> "login";
            default -> property;
        };
    }
}
