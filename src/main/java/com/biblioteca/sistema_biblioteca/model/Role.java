package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_role")
    private Integer id;

    @Column(name = "nome_role", nullable = false)
    private String nome;

    // ==========================
    // CONSTRUTORES
    // ==========================

    public Role() {}

    public Role(String nome) {
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
        return "Role{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}
