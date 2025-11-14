package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_OBRA")
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_obra")
    private Long id;

    @Column(name = "nome_obra", nullable = false)
    private String nome;

    // ===========================
    // Construtores
    // ===========================
    public Obra() {}

    public Obra(String nome) {
        this.nome = nome;
    }

    // ===========================
    // Getters / Setters
    // ===========================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public String toString() {
        return "Obra{id=" + id + ", nome='" + nome + "'}";
    }
}
