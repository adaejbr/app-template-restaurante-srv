package com.app.restaurante.srv.application.core.entity;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "cargo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Compara pela PK: a colecao de permissoes nao pode entrar em equals/hashCode.
@EqualsAndHashCode(of = "id")
// Deixa a colecao fora do log para nao disparar lazy loading no stack trace.
@ToString(of = {"id", "nome"})
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_setor", nullable = false,
        foreignKey = @ForeignKey(name = "fk_cargo_setor"))
    private Setor setor;

    // A tabela de juncao guarda "situacao"; so as ATIVA contam para o cargo.
    @OneToMany(mappedBy = "cargo", fetch = FetchType.LAZY)
    @SQLRestriction("situacao = 'ATIVA'")
    @Builder.Default
    private Set<PermissaoCargo> permissaoCargos = new HashSet<>();

    /** Projecao exigida pelo criterio de aceite da CARGO-01 (#65). */
    public Set<Permissao> getPermissoes() {
        return permissaoCargos.stream()
                .map(PermissaoCargo::getPermissao)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
