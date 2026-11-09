package com.app.restaurante.srv.application.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableFactoryTest {

    @Test
    void returnsDefaultsWhenFilterIsNull() {
        Pageable pageable = PageableFactory.from(null);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertTrue(pageable.getSort().isUnsorted());
    }

    @Test
    void clampsSizeToMaxAndAppliesDefaultsForNegativeValues() {
        BaseFilter filter = new BaseFilter();
        filter.setPage(-1);
        filter.setSize(500);

        Pageable pageable = PageableFactory.from(filter);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
    }

    @Test
    void parsesAscendingAndDescendingSortCorrectly() {
        BaseFilter ascFilter = new BaseFilter();
        ascFilter.setSort("nome,asc");
        Pageable ascPageable = PageableFactory.from(ascFilter);

        Sort.Order ascOrder = ascPageable.getSort().getOrderFor("nome");
        assertNotNull(ascOrder);
        assertEquals(Sort.Direction.ASC, ascOrder.getDirection());

        BaseFilter descFilter = new BaseFilter();
        descFilter.setSort("valorProduto,DESC");
        Pageable descPageable = PageableFactory.from(descFilter);

        Sort.Order descOrder = descPageable.getSort().getOrderFor("valorProduto");
        assertNotNull(descOrder);
        assertEquals(Sort.Direction.DESC, descOrder.getDirection());
    }

    @Test
    void returnsUnsortedWhenSortIsBlank() {
        BaseFilter filter = new BaseFilter();
        filter.setSort("   ");

        Pageable pageable = PageableFactory.from(filter);

        assertTrue(pageable.getSort().isUnsorted());
    }
}
