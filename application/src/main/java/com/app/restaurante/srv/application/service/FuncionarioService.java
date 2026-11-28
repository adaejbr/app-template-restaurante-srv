package com.app.restaurante.srv.application.service;

import com.ada.erp.cargo.exception.CargoNaoEncontradoException;
import com.ada.erp.shared.auditoria.Auditable;
import com.app.restaurante.srv.application.core.entity.Cargo;
import com.app.restaurante.srv.application.core.entity.CargoFuncionario;
import com.app.restaurante.srv.application.core.entity.Endereco;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.core.entity.Perfil;
import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.FuncionarioCreateRequest;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.dto.FuncionarioResponse;
import com.app.restaurante.srv.application.dto.FuncionarioUpdateRequest;
import com.app.restaurante.srv.application.exception.CpfDuplicadoException;
import com.app.restaurante.srv.application.exception.EnderecoNaoEncontradoException;
import com.app.restaurante.srv.application.exception.FuncionarioNaoEncontradoException;
import com.app.restaurante.srv.application.exception.LoginDuplicadoException;
import com.app.restaurante.srv.application.infra.repository.CargoFuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.CargoRepository;
import com.app.restaurante.srv.application.infra.repository.EnderecoRepository;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.restaurante.srv.application.infra.repository.PerfilRepository;
import com.app.restaurante.srv.application.infra.specification.FuncionarioSpecification;
import com.app.restaurante.srv.application.mapper.FuncionarioMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Serviço de gestão e listagem paginada de funcionários.
 */
@Service
@Validated
public class FuncionarioService {

    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "perfil.nome");

    private final FuncionarioRepository funcionarioRepository;
    private final PerfilRepository perfilRepository;
    private final EnderecoRepository enderecoRepository;
    private final CargoRepository cargoRepository;
    private final CargoFuncionarioRepository cargoFuncionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FuncionarioMapper funcionarioMapper;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            PerfilRepository perfilRepository,
            EnderecoRepository enderecoRepository,
            CargoRepository cargoRepository,
            CargoFuncionarioRepository cargoFuncionarioRepository,
            PasswordEncoder passwordEncoder,
            FuncionarioMapper funcionarioMapper) {
        this.funcionarioRepository = funcionarioRepository;
        this.perfilRepository = perfilRepository;
        this.enderecoRepository = enderecoRepository;
        this.cargoRepository = cargoRepository;
        this.cargoFuncionarioRepository = cargoFuncionarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.funcionarioMapper = funcionarioMapper;
    }

    @Transactional
    @Auditable(acao = "FUNCIONARIO_CRIAR")
    public FuncionarioResponse criar(@Valid FuncionarioCreateRequest request) {
        String cpf = request.cpf().replace(".", "").replace("-", "");
        String cpfComMascara = "%s.%s.%s-%s".formatted(
                cpf.substring(0, 3), cpf.substring(3, 6), cpf.substring(6, 9), cpf.substring(9));
        validarCpfUnico(cpf, null);
        validarCpfUnico(cpfComMascara, null);
        validarLoginUnico(request.email(), null);

        Endereco endereco = enderecoRepository.findById(request.enderecoId())
                .orElseThrow(EnderecoNaoEncontradoException::new);
        Perfil perfil = Perfil.builder()
                .nome(request.nome().trim())
                .cpf(cpf)
                .telefone(request.telefone())
                .endereco(endereco)
                .build();

        try {
            Funcionario funcionario = Funcionario.builder()
                    .perfil(perfilRepository.save(perfil))
                    .login(request.email())
                    .senha(passwordEncoder.encode(request.senha()))
                    .status(STATUS_ATIVO)
                    .build();
            return funcionarioMapper.toResponse(funcionarioRepository.saveAndFlush(funcionario));
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && violation.getConstraintName() != null) {
                    String constraint = violation.getConstraintName().toLowerCase(Locale.ROOT);
                    if (constraint.contains("uq_perfil_cpf")) {
                        throw new CpfDuplicadoException();
                    }
                    if (constraint.contains("uq_funcionario_login")) {
                        throw new LoginDuplicadoException();
                    }
                }
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<FuncionarioListResponse> listar(FuncionarioFilter filter) {
        Pageable pageable = resolvePageable(filter);
        Page<FuncionarioListResponse> page = funcionarioRepository
                .findAll(FuncionarioSpecification.from(filter), pageable)
                .map(FuncionarioListResponse::from);
        return PageResponse.from(page);
    }

    private Pageable resolvePageable(FuncionarioFilter filter) {
        Pageable base = PageableFactory.from(filter);
        Sort sort = base.getSort();
        if (sort.isUnsorted()) {
            return PageRequest.of(base.getPageNumber(), base.getPageSize(), DEFAULT_SORT);
        }
        List<Sort.Order> mappedOrders = sort.stream()
                .map(order -> order.withProperty(mapSortProperty(order.getProperty())))
                .toList();
        return PageRequest.of(base.getPageNumber(), base.getPageSize(), Sort.by(mappedOrders));
    }

    private String mapSortProperty(String property) {
        return switch (property) {
            case "nome" -> "perfil.nome";
            case "cpf" -> "perfil.cpf";
            case "email" -> "login";
            default -> property;
        };
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
