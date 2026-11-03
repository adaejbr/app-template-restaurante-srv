package com.app.restaurante.srv.application.core.filter;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

/**
 * Fábrica responsável por converter e validar parâmetros de paginação e ordenação a partir de um {@link BaseFilter}.
 */
public final class PageableFactory {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PageableFactory() {
    }

    /**
     * Converte o filtro base em um objeto {@link Pageable} do Spring Data.
     *
     * @param filter filtro contendo os parâmetros de paginação e ordenação
     * @return {@link Pageable} configurado
     */
    public static Pageable from(BaseFilter filter) {
        if (filter == null) {
            return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        }

        int page = (filter.getPage() != null && filter.getPage() >= 0) ? filter.getPage() : DEFAULT_PAGE;
        int size = (filter.getSize() != null && filter.getSize() > 0) ? filter.getSize() : DEFAULT_SIZE;

        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }

        Sort sort = parseSort(filter.getSort());
        return PageRequest.of(page, size, sort);
    }

    /**
     * Realiza o parsing e validação da string de ordenação no formato 'campo,direcao'.
     *
     * @param sortParam string contendo a ordenação (ex: 'nome,asc' ou 'id,desc')
     * @return objeto {@link Sort} correspondente ou {@link Sort#unsorted()} caso vazio/inválido
     */
    public static Sort parseSort(String sortParam) {
        if (!StringUtils.hasText(sortParam)) {
            return Sort.unsorted();
        }

        String[] parts = sortParam.split(",");
        String property = parts[0].trim();

        if (property.isEmpty()) {
            return Sort.unsorted();
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dirString = parts[1].trim();
            if ("desc".equalsIgnoreCase(dirString)) {
                direction = Sort.Direction.DESC;
            }
        }

        return Sort.by(direction, property);
    }
}
