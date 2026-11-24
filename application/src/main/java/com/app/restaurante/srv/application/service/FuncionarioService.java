package com.app.restaurante.srv.application.service;

import com.ada.erp.shared.auditoria.Auditable;
import com.ada.erp.usuario.exception.AutoAlteracaoSenhaNegadaException;
import com.ada.erp.usuario.exception.FuncionarioNaoEncontradoException;
import com.app.restaurante.srv.application.core.entity.Funcionario;
import com.app.restaurante.srv.application.core.filter.PageResponse;
import com.app.restaurante.srv.application.core.filter.PageableFactory;
import com.app.restaurante.srv.application.dto.FuncionarioFilter;
import com.app.restaurante.srv.application.dto.FuncionarioListResponse;
import com.app.restaurante.srv.application.infra.repository.FuncionarioRepository;
import com.app.restaurante.srv.application.infra.specification.FuncionarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de consulta, listagem paginada e administração de senha de funcionários.
 */
@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, PasswordEncoder passwordEncoder) {
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
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
     * Gerente define uma senha temporaria para outro funcionario, sem a senha
     * atual dele (FUNC-05, #67). O funcionario fica marcado para trocar a senha
     * no proximo acesso, ja que o gerente passa a conhece-la.
     *
     * <p>O retorno e o funcionario alvo, para a auditoria registrar o id da
     * conta afetada; quem alterou vem do contexto de seguranca.
     *
     * @param id         funcionario alvo
     * @param novaSenha  senha em texto puro, ja validada pelo {@code @SenhaForte}
     * @param loginAutor login de quem executa a acao
     * @throws FuncionarioNaoEncontradoException alvo inexistente
     * @throws AutoAlteracaoSenhaNegadaException alvo e o proprio autor
     */
    @Transactional
    @Auditable(acao = "PASSWORD_CHANGE")
    public Funcionario alterarSenhaPorGerente(Integer id, String novaSenha, String loginAutor) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(FuncionarioNaoEncontradoException::new);
        if (funcionario.getLogin().equalsIgnoreCase(loginAutor)) {
            throw new AutoAlteracaoSenhaNegadaException();
        }
        funcionario.definirSenhaTemporaria(passwordEncoder.encode(novaSenha));
        return funcionarioRepository.save(funcionario);
    }
}
