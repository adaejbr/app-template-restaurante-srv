package com.app.restaurante.srv.application.service;

import com.ada.erp.cargo.exception.CargoNaoEncontradoException;
import com.ada.erp.shared.auditoria.Auditable;
import com.app.restaurante.srv.application.core.entity.Cargo;
import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.core.entity.Perfil;
import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.dto.FuncionarioResponse;
import com.app.restaurante.srv.application.dto.FuncionarioUpdateRequest;
import com.app.restaurante.srv.application.exception.CpfDuplicadoException;
import com.app.restaurante.srv.application.exception.FuncionarioNaoEncontradoException;
import com.app.restaurante.srv.application.exception.LoginDuplicadoException;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.CargoRepository;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.PerfilRepository;
import com.app.restaurante.srv.application.infra.specification.FuncionarioSpecification;
import com.app.restaurante.srv.application.mapper.FuncionarioMapper;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de gestão e listagem paginada de funcionários.
 */
@Service
public class FuncionarioService {

    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";

    private final FuncionarioRepository funcionarioRepository;
    private final PerfilRepository perfilRepository;
    private final CargoRepository cargoRepository;
    private final CargoFuncionarioRepository cargoFuncionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FuncionarioMapper funcionarioMapper;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            PerfilRepository perfilRepository,
            CargoRepository cargoRepository,
            CargoFuncionarioRepository cargoFuncionarioRepository,
            PasswordEncoder passwordEncoder,
            FuncionarioMapper funcionarioMapper) {
        this.funcionarioRepository = funcionarioRepository;
        this.perfilRepository = perfilRepository;
        this.cargoRepository = cargoRepository;
        this.cargoFuncionarioRepository = cargoFuncionarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.funcionarioMapper = funcionarioMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FuncionarioListResponse> listar(FuncionarioFilter filter) {
        Pageable pageable = PageableFactory.from(filter);
        Page<FuncionarioListResponse> page = funcionarioRepository
                .findAll(FuncionarioSpecification.from(filter), pageable)
                .map(FuncionarioListResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Atualiza dados cadastrais, cargo e opcionalmente a senha de um funcionário (FUNC-03, #70).
     * Quando a senha não é informada (nula ou em branco), preserva o hash atual.
     */
    @Transactional
    @Auditable(acao = "FUNCIONARIO_ATUALIZAR")
    public FuncionarioResponse atualizar(Integer id, FuncionarioUpdateRequest request) {
        Funcionario funcionario = funcionarioRepository.findDetalhadoById(id)
                .orElseThrow(FuncionarioNaoEncontradoException::new);

        Perfil perfil = funcionario.getPerfil();
        String cpf = request.cpf().trim();
        String login = request.login().trim();

        validarCpfUnico(cpf, perfil.getId());
        validarLoginUnico(login, funcionario.getId());
        Cargo cargo = cargoRepository.findById(request.idCargo())
                .orElseThrow(CargoNaoEncontradoException::new);

        perfil.setNome(request.nome().trim());
        perfil.setCpf(cpf);
        perfil.setTelefone(request.telefone());
        perfilRepository.save(perfil);

        funcionario.setLogin(login);
        if (request.status() != null && !request.status().isBlank()) {
            funcionario.setStatus(request.status().trim());
        }
        if (request.senha() != null && !request.senha().isBlank()) {
            funcionario.setSenha(passwordEncoder.encode(request.senha()));
        }
        funcionarioRepository.save(funcionario);

        atualizarCargoAtivo(funcionario, cargo);

        return funcionarioMapper.toResponse(funcionario, cargo);
    }

    private void validarCpfUnico(String cpf, Integer idPerfilAtual) {
        Perfil existente = perfilRepository.findByCpf(cpf);
        if (existente != null && !existente.getId().equals(idPerfilAtual)) {
            throw new CpfDuplicadoException();
        }
    }

    private void validarLoginUnico(String login, Integer idFuncionarioAtual) {
        Funcionario existente = funcionarioRepository.findByLogin(login);
        if (existente != null && !existente.getId().equals(idFuncionarioAtual)) {
            throw new LoginDuplicadoException();
        }
    }

    private void atualizarCargoAtivo(Funcionario funcionario, Cargo novoCargo) {
        List<CargoFuncionario> vinculos = cargoFuncionarioRepository.findByIdFuncionario(funcionario.getId());
        CargoFuncionario vinculoAlvo = null;

        for (CargoFuncionario vinculo : vinculos) {
            if (vinculo.getIdCargo().equals(novoCargo.getId())) {
                vinculo.setStatus(STATUS_ATIVO);
                vinculoAlvo = vinculo;
            } else if (STATUS_ATIVO.equalsIgnoreCase(vinculo.getStatus())) {
                vinculo.setStatus(STATUS_INATIVO);
            }
        }

        if (!vinculos.isEmpty()) {
            cargoFuncionarioRepository.saveAll(vinculos);
        }

        if (vinculoAlvo == null) {
            CargoFuncionario novoVinculo = CargoFuncionario.builder()
                    .idCargo(novoCargo.getId())
                    .idFuncionario(funcionario.getId())
                    .status(STATUS_ATIVO)
                    .cargo(novoCargo)
                    .funcionario(funcionario)
                    .build();
            cargoFuncionarioRepository.save(novoVinculo);
        }
    }
}
