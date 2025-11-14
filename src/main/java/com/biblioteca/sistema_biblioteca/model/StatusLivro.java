package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_STATUS_LIVRO")
public class StatusLivro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_status")
    private Integer id;

    @Column(name = "nome_status", nullable = false, unique = true)
    private String nome;

    // =================================
    // Construtores
    // =================================

    public StatusLivro() {}

    public StatusLivro(String nome) {
        this.nome = nome;
    }

    // =================================
    // Getters e Setters
    // =================================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // =================================
    // toString (ajuda no debug)
    // =================================

    @Override
    public String toString() {
        return "StatusLivro{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}
