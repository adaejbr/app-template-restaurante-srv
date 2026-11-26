package com.app.restaurante.srv.application.infra.repository;

import java.util.List;
import java.util.Optional;
import com.app.restaurante.srv.application.core.entity.Cargo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Integer>,
        JpaSpecificationExecutor<Cargo> {

    @EntityGraph(attributePaths = {"setor", "permissaoCargos", "permissaoCargos.permissao"})
    List<Cargo> findByStatusOrderByIdAsc(String status);

    @EntityGraph(attributePaths = {"setor", "permissaoCargos", "permissaoCargos.permissao"})
    Optional<Cargo> findByIdAndStatus(Integer id, String status);

    boolean existsByNomeIgnoreCaseAndSetorId(String nome, Integer idSetor);

    boolean existsByNomeIgnoreCaseAndSetorIdAndIdNot(String nome, Integer idSetor, Integer id);
}
