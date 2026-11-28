package com.app.restaurante.srv.application.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30)")
    private Cargo cargo = Cargo.CLIENTE;

    protected Usuario() {}

    public Usuario(String nome, String email) {
        this(nome, email, Cargo.CLIENTE);
    }

    public Usuario(String nome, String email, Cargo cargo) {
        this.nome = nome;
        this.email = email;
        this.cargo = java.util.Objects.requireNonNull(cargo);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Cargo getCargo() { return cargo; }
}
