package com.ada.erp.cargo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.ada.erp.cargo.exception.CargoNaoEncontradoException;
import com.ada.erp.cargo.exception.PermissoesInexistentesException;
import com.ada.erp.shared.auditoria.Auditable;
import com.ada.erp.usuario.dto.PermissaoResponse;
import com.ada.erp.usuario.mapper.PermissaoMapper;
import com.app.restaurante.srv.application.core.entity.Cargo;
import com.app.restaurante.srv.application.core.entity.Permissao;
import com.app.restaurante.srv.application.core.entity.PermissaoCargo;
import com.app.restaurante.srv.application.infra.repository.CargoRepository;
import com.app.restaurante.srv.application.infra.repository.PermissaoCargoRepository;
import com.app.restaurante.srv.application.infra.repository.PermissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Leitura e substituicao do conjunto de permissoes de um cargo (CARGO-03, #57).
 *
 * <p>A substituicao joga na coluna "situacao" em vez de apagar a linha: quem sai
 * vira INATIVA, quem entra vira ATIVA (reativando o vinculo antigo se existir).
 * Assim a tabela guarda o historico e nada estoura a PK composta, ja que a
 * leitura ({@code Cargo.getPermissoes} e o login) filtra por ATIVA.
 */
@Service
public class CargoPermissaoService {

    private static final String SITUACAO_ATIVA = "ATIVA";
    private static final String SITUACAO_INATIVA = "INATIVA";

    private final CargoRepository cargoRepository;
    private final PermissaoRepository permissaoRepository;
    private final PermissaoCargoRepository permissaoCargoRepository;
    private final PermissaoMapper permissaoMapper;

    public CargoPermissaoService(CargoRepository cargoRepository,
            PermissaoRepository permissaoRepository,
            PermissaoCargoRepository permissaoCargoRepository,
            PermissaoMapper permissaoMapper) {
        this.cargoRepository = cargoRepository;
        this.permissaoRepository = permissaoRepository;
        this.permissaoCargoRepository = permissaoCargoRepository;
        this.permissaoMapper = permissaoMapper;
    }

    @Transactional(readOnly = true)
    public List<PermissaoResponse> buscarPermissoes(Integer idCargo) {
        return buscarCargo(idCargo).getPermissoes().stream()
                .sorted(Comparator.comparing(Permissao::getId))
                .map(permissaoMapper::toResponse)
                .toList();
    }

    @Transactional
    @Auditable(acao = "CARGO_PERMISSOES_ATUALIZAR")
    public Cargo atualizarPermissoes(Integer idCargo, List<Integer> idsPermissoes) {
        Cargo cargo = buscarCargo(idCargo);
        Set<Integer> desejados = new LinkedHashSet<>(idsPermissoes);
        Map<Integer, Permissao> permissoes = validarPermissoes(desejados);

        Map<Integer, PermissaoCargo> vinculos = new HashMap<>();
        for (PermissaoCargo vinculo : permissaoCargoRepository.findByCargo(idCargo)) {
            vinculos.put(vinculo.getIdPermissao(), vinculo);
        }

        // Quem saiu do payload perde a ATIVA, mas a linha fica para o historico.
        for (Integer idPermissao : vinculos.keySet()) {
            if (!desejados.contains(idPermissao)) {
                vinculos.get(idPermissao).setSituacao(SITUACAO_INATIVA);
            }
        }

        List<PermissaoCargo> novos = new ArrayList<>();
        for (Integer idPermissao : desejados) {
            PermissaoCargo vinculo = vinculos.get(idPermissao);
            if (vinculo == null) {
                // Preenche as referencias tambem: sem elas o Cargo.getPermissoes()
                // devolveria null para o vinculo recem-criado ainda na memoria.
                novos.add(PermissaoCargo.builder()
                        .idPermissao(idPermissao)
                        .idCargo(idCargo)
                        .situacao(SITUACAO_ATIVA)
                        .permissao(permissoes.get(idPermissao))
                        .cargo(cargo)
                        .build());
            } else {
                vinculo.setSituacao(SITUACAO_ATIVA);
            }
        }
        permissaoCargoRepository.saveAll(novos);
        // Grava antes de devolver: o lazy load da colecao do Cargo nao dispara
        // flush, entao uma leitura na mesma transacao (como no teste de
        // integracao do criterio) enxergaria o estado anterior.
        permissaoCargoRepository.flush();
        return cargo;
    }

    private Cargo buscarCargo(Integer idCargo) {
        return cargoRepository.findById(idCargo).orElseThrow(CargoNaoEncontradoException::new);
    }

    /** Valida o payload inteiro antes de mexer em qualquer vinculo. */
    private Map<Integer, Permissao> validarPermissoes(Set<Integer> desejados) {
        Map<Integer, Permissao> porId = permissaoRepository.findAllById(desejados).stream()
                .collect(Collectors.toMap(Permissao::getId, permissao -> permissao));
        List<Integer> faltantes = desejados.stream()
                .filter(idPermissao -> !porId.containsKey(idPermissao))
                .toList();
        if (!faltantes.isEmpty()) {
            throw new PermissoesInexistentesException(faltantes);
        }
        return porId;
    }
}
