package com.app.restaurante.srv.application.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "funcionario",
    indexes = @Index(name = "idx_funcionario_status", columnList = "status"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perfil", nullable = false, unique = true,
        foreignKey = @ForeignKey(name = "fk_funcionario_perfil"))
    private Perfil perfil;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(nullable = false, length = 30)
    private String status;

    /**
     * Senha definida por terceiro (gerente); o funcionario deve criar uma senha
     * pessoal no proximo acesso. Alterada apenas pelos metodos de senha abaixo.
     */
    @Column(name = "deve_alterar_senha", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean deveAlterarSenha = false;

    @OneToMany(mappedBy = "funcionario", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CargoFuncionario> cargos = new HashSet<>();

    /**
     * Senha definida por quem administra a conta (FUNC-05, cadastro e edicao
     * por gerente): quem definiu conhece a senha, entao o funcionario deve
     * troca-la no proximo acesso.
     *
     * @param hash senha ja codificada pelo {@code PasswordEncoder}
     */
    public void definirSenhaTemporaria(String hash) {
        this.senha = hash;
        this.deveAlterarSenha = true;
    }

    /**
     * Senha escolhida pelo proprio funcionario (troca da propria senha, RF002):
     * encerra a obrigacao de troca.
     *
     * @param hash senha ja codificada pelo {@code PasswordEncoder}
     */
    public void definirSenhaPessoal(String hash) {
        this.senha = hash;
        this.deveAlterarSenha = false;
    }
}
