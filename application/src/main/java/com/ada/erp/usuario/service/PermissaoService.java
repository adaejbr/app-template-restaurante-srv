package com.ada.erp.usuario.service;

import java.util.List;
import com.ada.erp.shared.auditoria.Auditable;
import com.ada.erp.usuario.dto.PermissaoCreateRequest;
import com.ada.erp.usuario.dto.PermissaoResponse;
import com.ada.erp.usuario.dto.PermissaoUpdateRequest;
import com.ada.erp.usuario.exception.ChaveImutavelException;
import com.ada.erp.usuario.exception.PermissaoDuplicadaException;
import com.ada.erp.usuario.exception.PermissaoEmUsoException;
import com.ada.erp.usuario.exception.PermissaoNaoEncontradaException;
import com.ada.erp.usuario.mapper.PermissaoMapper;
import com.ada.erp.usuario.specification.PermissaoSpecification;
import com.app.restaurante.srv.application.core.entity.Permissao;
import com.app.restaurante.srv.application.infra.repository.PermissaoCargoRepository;
import com.app.restaurante.srv.application.infra.repository.PermissaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de permissao com chave imutavel (PERM-02). */
@Service
public class PermissaoService {

    private final PermissaoRepository repository;
    private final PermissaoCargoRepository permissaoCargoRepository;
    private final PermissaoMapper mapper;

    public PermissaoService(PermissaoRepository repository,
            PermissaoCargoRepository permissaoCargoRepository,
            PermissaoMapper mapper) {
        this.repository = repository;
        this.permissaoCargoRepository = permissaoCargoRepository;
        this.mapper = mapper;
    }

    @Transactional
    @Auditable(acao = "PERMISSAO_CRIAR")
    public PermissaoResponse criar(PermissaoCreateRequest request) {
        if (repository.existsByChave(request.chave())) {
            throw new PermissaoDuplicadaException();
        }
        Permissao permissao = mapper.toEntity(request);
        try {
            repository.saveAndFlush(permissao);
        } catch (DataIntegrityViolationException exception) {
            // Corrida entre a checagem acima e o insert: a unique decide.
            throw new PermissaoDuplicadaException();
        }
        return mapper.toResponse(permissao);
    }

    @Transactional(readOnly = true)
    public List<PermissaoResponse> listar(String q) {
        return repository.findAll(PermissaoSpecification.comBusca(q)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PermissaoResponse buscar(Integer id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    @Auditable(acao = "PERMISSAO_ATUALIZAR")
    public PermissaoResponse atualizar(Integer id, PermissaoUpdateRequest request) {
        Permissao permissao = buscarEntidade(id);
        if (request.chave() != null && !request.chave().equals(permissao.getChave())) {
            throw new ChaveImutavelException();
        }
        permissao.setNome(request.nome());
        permissao.setDescricao(request.descricao());
        repository.saveAndFlush(permissao);
        return mapper.toResponse(permissao);
    }

    @Transactional
    @Auditable(acao = "PERMISSAO_EXCLUIR")
    public void excluir(Integer id) {
        Permissao permissao = buscarEntidade(id);
        if (permissaoCargoRepository.existsByIdPermissao(id)) {
            throw new PermissaoEmUsoException();
        }
        repository.delete(permissao);
    }

    private Permissao buscarEntidade(Integer id) {
        return repository.findById(id).orElseThrow(PermissaoNaoEncontradaException::new);
    }
}
