package com.app.restaurante.srv.application.infra.repository;

import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.CargoFuncionarioId;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoFuncionarioRepository extends JpaRepository<CargoFuncionario, CargoFuncionarioId> {

    /** Busca o cargo ativo de um funcionário (usado no login - task j-05). */
    @EntityGraph(attributePaths = "cargo")
    Optional<CargoFuncionario> findFirstByIdFuncionarioAndStatus(Integer idFuncionario, String status);
}