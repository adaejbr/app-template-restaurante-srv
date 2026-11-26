package com.ada.erp.cargo.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.ada.erp.cargo.dto.CargoCreateRequest;
import com.ada.erp.cargo.dto.CargoResponse;
import com.ada.erp.cargo.dto.CargoUpdateRequest;
import com.ada.erp.cargo.exception.CargoDuplicadoException;
import com.ada.erp.cargo.exception.CargoNaoEncontradoException;
import com.ada.erp.cargo.exception.SetorNaoEncontradoException;
import com.ada.erp.cargo.mapper.CargoMapper;
import com.ada.erp.shared.auditoria.Auditable;
import com.app.restaurante.srv.application.core.entity.Cargo;
import com.app.restaurante.srv.application.core.entity.PermissaoCargo;
import com.app.restaurante.srv.application.core.entity.Setor;
import com.app.restaurante.srv.application.infra.repository.CargoRepository;
import com.app.restaurante.srv.application.infra.repository.PermissaoCargoRepository;
import com.app.restaurante.srv.application.infra.repository.SetorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de cargo com permissoes embutidas e desativacao logica (CARGO-02, #66). */
@Service
public class CargoFuncionarioService {

    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";
    private static final String SITUACAO_ATIVA = "ATIVA";

    private final CargoRepository cargoRepository;
    private final SetorRepository setorRepository;
    private final PermissaoCargoRepository permissaoCargoRepository;
    private final CargoPermissaoService cargoPermissaoService;
    private final CargoMapper cargoMapper;

    public CargoFuncionarioService(CargoRepository cargoRepository,
            SetorRepository setorRepository,
            PermissaoCargoRepository permissaoCargoRepository,
            CargoPermissaoService cargoPermissaoService,
            CargoMapper cargoMapper) {
        this.cargoRepository = cargoRepository;
        this.setorRepository = setorRepository;
        this.permissaoCargoRepository = permissaoCargoRepository;
        this.cargoPermissaoService = cargoPermissaoService;
        this.cargoMapper = cargoMapper;
    }

    @Transactional(readOnly = true)
    public List<CargoResponse> listar() {
        return cargoRepository.findByStatusOrderByIdAsc(STATUS_ATIVO).stream()
                .map(cargoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CargoResponse buscar(Integer id) {
        return cargoMapper.toResponse(buscarCargoAtivo(id));
    }

    @Transactional
    @Auditable(acao = "CARGO_CRIAR")
    public CargoResponse criar(CargoCreateRequest request) {
        String nome = request.nome().trim();
        Setor setor = buscarSetor(request.idSetor());
        if (cargoRepository.existsByNomeIgnoreCaseAndSetorId(nome, setor.getId())) {
            throw new CargoDuplicadoException();
        }

        Cargo cargo = cargoMapper.toEntity(request);
        cargo.setNome(nome);
        cargo.setSetor(setor);
        cargo.setStatus(STATUS_ATIVO);
        cargoRepository.saveAndFlush(cargo);

        if (request.permissoes() != null) {
            sincronizarPermissoes(cargo, request.permissoes());
        }
        return cargoMapper.toResponse(cargo);
    }

    @Transactional
    @Auditable(acao = "CARGO_ATUALIZAR")
    public CargoResponse atualizar(Integer id, CargoUpdateRequest request) {
        Cargo cargo = buscarCargoAtivo(id);
        String nome = request.nome().trim();
        Setor setor = buscarSetor(request.idSetor());
        if (cargoRepository.existsByNomeIgnoreCaseAndSetorIdAndIdNot(nome, setor.getId(), id)) {
            throw new CargoDuplicadoException();
        }

        cargo.setNome(nome);
        cargo.setSetor(setor);
        cargoRepository.saveAndFlush(cargo);

        if (request.permissoes() != null) {
            sincronizarPermissoes(cargo, request.permissoes());
        }
        return cargoMapper.toResponse(cargo);
    }

    @Transactional
    @Auditable(acao = "CARGO_DESATIVAR")
    public void desativar(Integer id) {
        Cargo cargo = buscarCargoAtivo(id);
        cargo.setStatus(STATUS_INATIVO);
        cargoRepository.saveAndFlush(cargo);
    }

    private Cargo buscarCargoAtivo(Integer id) {
        return cargoRepository.findByIdAndStatus(id, STATUS_ATIVO)
                .orElseThrow(CargoNaoEncontradoException::new);
    }

    private Setor buscarSetor(Integer idSetor) {
        return setorRepository.findById(idSetor)
                .orElseThrow(SetorNaoEncontradoException::new);
    }

    private void sincronizarPermissoes(Cargo cargo, List<Integer> idsPermissoes) {
        cargoPermissaoService.atualizarPermissoes(cargo.getId(), idsPermissoes);
        Set<PermissaoCargo> ativas = permissaoCargoRepository.findByCargo(cargo.getId()).stream()
                .filter(vinculo -> SITUACAO_ATIVA.equals(vinculo.getSituacao()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        cargo.setPermissaoCargos(ativas);
    }
}
