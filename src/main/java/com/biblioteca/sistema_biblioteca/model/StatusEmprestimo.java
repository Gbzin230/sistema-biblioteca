package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_status_emprestimo")
public class StatusEmprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_status")
    private Integer id;

    @Column(name = "nome_status", nullable = false)
    private String nome;

    public StatusEmprestimo() {}

    public StatusEmprestimo(String nome) {
        this.nome = nome;
    }

    // ==========================
    // GETTERS E SETTERS
    // ==========================

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

    @Override
    public String toString() {
        return "StatusEmprestimo{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}
