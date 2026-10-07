package com.app.template._1.restaurante.srv.application.usuario;

import jakarta.persistence.*;

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
