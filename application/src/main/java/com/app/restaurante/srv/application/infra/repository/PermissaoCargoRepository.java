package com.app.restaurante.srv.application.infra.repository;

import com.app.restaurante.srv.application.core.entity.PermissaoCargo;
import com.app.restaurante.srv.application.core.entity.PermissaoCargoId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissaoCargoRepository extends JpaRepository<PermissaoCargo, PermissaoCargoId> {

    // Chaves ativas do cargo, usadas como authorities no login (PERM-02).
    @Query("""
            SELECT pc.permissao.chave
            FROM PermissaoCargo pc
            WHERE pc.idCargo = :idCargo
              AND pc.situacao = 'ATIVA'
            """)
    List<String> findChavesAtivasByCargo(@Param("idCargo") Integer idCargo);

    // Todos os vinculos do cargo, com ou sem situacao (CARGO-03): a substituicao
    // precisa reaproveitar a linha existente em vez de duplicar a PK composta.
    @Query("""
            SELECT pc
            FROM PermissaoCargo pc
            JOIN FETCH pc.permissao
            WHERE pc.idCargo = :idCargo
            """)
    List<PermissaoCargo> findByCargo(@Param("idCargo") Integer idCargo);

    // Bloqueia exclusao de permissao vinculada a algum cargo (PERM-02).
    boolean existsByIdPermissao(Integer idPermissao);
}
