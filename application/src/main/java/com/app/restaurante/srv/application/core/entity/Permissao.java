package com.app.restaurante.srv.application.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

// Permissao do RBAC: "chave" identifica a autorizacao no codigo, "nome" e o rotulo humano.
// Imutavel em id e chave: sem setter, entao alterar no codigo nao compila.
// nome e descricao tem setter porque o CRUD da PERM-02 edita os dois.
// Nao usar @Immutable do Hibernate: deixa read-only e o PUT da PERM-02 nao salva nada.
@Entity
@Table(name = "permissao")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// Compara pela PK. Comparar por todos os campos quebra quando entrar em Set<Permissao>.
@EqualsAndHashCode(of = "id")
// Deixa descricao fora do log pra nao arrastar lazy loading pro stack trace.
@ToString(of = {"id", "chave", "nome"})
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100, unique = true)
    private String chave;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
