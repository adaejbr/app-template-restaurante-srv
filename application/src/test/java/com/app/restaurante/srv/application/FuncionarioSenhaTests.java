package com.app.restaurante.srv.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.restaurante.srv.application.core.entity.Funcionario;
import org.junit.jupiter.api.Test;

// Transicoes da flag deve_alterar_senha (FUNC-05, #67). A troca da propria
// senha (RF002) usara definirSenhaPessoal para encerrar a obrigacao.
class FuncionarioSenhaTests {

    @Test
    void novoFuncionarioNaoPrecisaTrocarSenha() {
        assertThat(Funcionario.builder().build().isDeveAlterarSenha()).isFalse();
        assertThat(new Funcionario().isDeveAlterarSenha()).isFalse();
    }

    @Test
    void senhaTemporariaExigeTrocaNoProximoAcesso() {
        Funcionario funcionario = Funcionario.builder().senha("hash-antigo").build();

        funcionario.definirSenhaTemporaria("hash-temporario");

        assertThat(funcionario.getSenha()).isEqualTo("hash-temporario");
        assertThat(funcionario.isDeveAlterarSenha()).isTrue();
    }

    @Test
    void senhaPessoalEncerraAObrigacaoDeTroca() {
        Funcionario funcionario = Funcionario.builder().build();
        funcionario.definirSenhaTemporaria("hash-temporario");

        funcionario.definirSenhaPessoal("hash-pessoal");

        assertThat(funcionario.getSenha()).isEqualTo("hash-pessoal");
        assertThat(funcionario.isDeveAlterarSenha()).isFalse();
    }
}
