package com.ada.erp.restaurante.service;

import com.ada.erp.restaurante.dto.RestauranteCnpjResponse;
import com.ada.erp.restaurante.dto.RestauranteRequest;
import com.ada.erp.restaurante.dto.RestauranteResponse;
import com.ada.erp.restaurante.exception.CnpjDuplicadoException;
import com.ada.erp.restaurante.exception.CnpjInvalidoException;
import com.ada.erp.restaurante.exception.IdDivergenteException;
import com.ada.erp.restaurante.exception.RestauranteNaoEncontradoException;
import com.ada.erp.shared.validation.CnpjSupport;
import com.app.restaurante.srv.application.core.entity.Restaurante;
import com.app.restaurante.srv.application.infra.repository.RestauranteRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Restaurante singleton: existe um unico registro, criado pelo seed e
 * atualizado no lugar. Nao ha criacao pela API.
 */
@Service
public class RestauranteService {
    private final RestauranteRepository repository;

    public RestauranteService(RestauranteRepository repository) {
        this.repository = repository;
    }

    /** Configuracao atual do restaurante. 404 enquanto ela nao existir. */
    @Transactional(readOnly = true)
    public RestauranteResponse get() {
        return toResponse(buscarSingleton());
    }

    /** Atualiza o registro existente. Nunca cria um restaurante novo. */
    @Transactional
    public RestauranteResponse update(RestauranteRequest request) {
        Restaurante restaurante = buscarSingleton();
        if (!restaurante.getId().equals(request.id())) {
            throw new IdDivergenteException();
        }
        if (!CnpjSupport.isValid(request.cnpj())) {
            throw new CnpjInvalidoException();
        }
        String canonical = CnpjSupport.normalize(request.cnpj());
        if (repository.existsByCanonicalCnpjAndIdNot(canonical, restaurante.getId())) {
            throw new CnpjDuplicadoException();
        }
        restaurante.setNome(request.nome());
        restaurante.setCnpj(canonical);
        restaurante.setTelefone(request.telefone());
        try {
            // Flush here so a concurrent unique-key violation is mapped before leaving the service.
            repository.saveAndFlush(restaurante);
        } catch (DataIntegrityViolationException exception) {
            if (isCnpjConflict(exception)) {
                throw new CnpjDuplicadoException();
            }
            throw exception;
        }
        return toResponse(restaurante);
    }

    @Transactional
    public RestauranteCnpjResponse update(Integer id, String cnpj) {
        if (!CnpjSupport.isValid(cnpj)) {
            throw new CnpjInvalidoException();
        }
        String canonical = CnpjSupport.normalize(cnpj);
        Restaurante restaurante = repository.findById(id).orElseThrow(RestauranteNaoEncontradoException::new);
        if (repository.existsByCanonicalCnpjAndIdNot(canonical, id)) {
            throw new CnpjDuplicadoException();
        }
        restaurante.setCnpj(canonical);
        try {
            // Flush here so a concurrent unique-key violation is mapped before leaving the service.
            repository.saveAndFlush(restaurante);
        } catch (DataIntegrityViolationException exception) {
            if (isCnpjConflict(exception)) {
                throw new CnpjDuplicadoException();
            }
            throw exception;
        }
        return new RestauranteCnpjResponse(restaurante.getId(), restaurante.getCnpj());
    }

    private Restaurante buscarSingleton() {
        return repository.findFirstByOrderByIdAsc()
                .orElseThrow(RestauranteNaoEncontradoException::new);
    }

    private RestauranteResponse toResponse(Restaurante restaurante) {
        return new RestauranteResponse(restaurante.getId(), restaurante.getNome(),
                restaurante.getCnpj(), restaurante.getTelefone());
    }

    private boolean isCnpjConflict(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null
                    && violation.getConstraintName().contains("uq_restaurante_cnpj")) {
                return true;
            }
        }
        return false;
    }
}
