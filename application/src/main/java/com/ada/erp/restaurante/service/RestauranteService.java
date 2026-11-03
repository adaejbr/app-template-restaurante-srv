package com.ada.erp.restaurante.service;

import com.ada.erp.restaurante.dto.RestauranteCnpjResponse;
import com.ada.erp.restaurante.exception.CnpjDuplicadoException;
import com.ada.erp.restaurante.exception.CnpjInvalidoException;
import com.ada.erp.restaurante.exception.RestauranteNaoEncontradoException;
import com.ada.erp.shared.validation.CnpjSupport;
import com.app.restaurante.srv.application.core.entity.Restaurante;
import com.app.restaurante.srv.application.infra.repository.RestauranteRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestauranteService {
    private final RestauranteRepository repository;

    public RestauranteService(RestauranteRepository repository) {
        this.repository = repository;
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
