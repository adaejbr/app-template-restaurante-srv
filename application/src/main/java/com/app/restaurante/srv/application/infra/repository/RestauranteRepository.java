package com.app.restaurante.srv.application.infra.repository;

import com.app.restaurante.srv.application.core.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    Restaurante findByCnpj(String cnpj);

    @Query("""
            select (count(r) > 0) from Restaurante r
            where upper(replace(replace(replace(trim(r.cnpj), '.', ''), '/', ''), '-', '')) = :cnpj
              and r.id <> :id
            """)
    boolean existsByCanonicalCnpjAndIdNot(@Param("cnpj") String cnpj, @Param("id") Integer id);
}
