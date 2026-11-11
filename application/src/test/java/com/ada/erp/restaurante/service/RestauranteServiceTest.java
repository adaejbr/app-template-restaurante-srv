package com.ada.erp.restaurante.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;
import com.ada.erp.restaurante.exception.CnpjDuplicadoException;
import com.ada.erp.restaurante.exception.CnpjInvalidoException;
import com.ada.erp.restaurante.exception.RestauranteNaoEncontradoException;
import com.app.restaurante.srv.application.core.entity.Restaurante;
import com.app.restaurante.srv.application.infra.repository.RestauranteRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class RestauranteServiceTest {
    @Mock
    private RestauranteRepository repository;

    private RestauranteService service;

    @BeforeEach
    void setUp() {
        service = new RestauranteService(repository);
    }

    @Test
    void normalizesBeforeCheckingUniquenessAndFlushing() {
        var restaurante = Restaurante.builder().id(1).cnpj("04252011000110").build();
        when(repository.findById(1)).thenReturn(Optional.of(restaurante));

        var result = service.update(1, "11.222.333/0001-81");

        assertEquals("11222333000181", result.cnpj());
        assertEquals(1, result.id());
        verify(repository).existsByCanonicalCnpjAndIdNot("11222333000181", 1);
        verify(repository).saveAndFlush(restaurante);
    }

    @Test
    void invalidInputDoesNotAccessPersistence() {
        assertThrows(CnpjInvalidoException.class, () -> service.update(1, "11222333000180"));
        assertThrows(CnpjInvalidoException.class, () -> service.update(1, null));
        verifyNoInteractions(repository);
    }

    @Test
    void duplicateDoesNotMutateManagedEntity() {
        var restaurante = Restaurante.builder().id(1).cnpj("04252011000110").build();
        when(repository.findById(1)).thenReturn(Optional.of(restaurante));
        when(repository.existsByCanonicalCnpjAndIdNot("11222333000181", 1)).thenReturn(true);

        assertThrows(CnpjDuplicadoException.class, () -> service.update(1, "11.222.333/0001-81"));
        assertEquals("04252011000110", restaurante.getCnpj());
        verify(repository).findById(1);
        verify(repository).existsByCanonicalCnpjAndIdNot("11222333000181", 1);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void missingRestaurantReturnsNotFound() {
        when(repository.findById(9)).thenReturn(Optional.empty());
        assertThrows(RestauranteNaoEncontradoException.class, () -> service.update(9, "11222333000181"));
        verify(repository).findById(9);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void concurrentCnpjConstraintViolationIsTranslatedToConflict() {
        var restaurante = Restaurante.builder().id(1).build();
        when(repository.findById(1)).thenReturn(Optional.of(restaurante));
        var sql = new ConstraintViolationException("duplicate", new SQLException("duplicate", "23000", 1062),
                "restaurante.uq_restaurante_cnpj");
        when(repository.saveAndFlush(restaurante)).thenThrow(new DataIntegrityViolationException("duplicate", sql));
        assertThrows(CnpjDuplicadoException.class, () -> service.update(1, "11222333000181"));
    }

    @Test
    void unrelatedIntegrityFailureIsNotReportedAsCnpjConflict() {
        var restaurante = Restaurante.builder().id(1).build();
        when(repository.findById(1)).thenReturn(Optional.of(restaurante));
        var failure = new DataIntegrityViolationException("foreign key");
        when(repository.saveAndFlush(restaurante)).thenThrow(failure);
        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> service.update(1, "11222333000181")));
    }
}
