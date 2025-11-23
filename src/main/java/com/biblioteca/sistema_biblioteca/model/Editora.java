package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_editora")
public class Editora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_editora")
    private Long id;

    @Column(name = "nome_editora", nullable = false, length = 100)
    private String nome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
