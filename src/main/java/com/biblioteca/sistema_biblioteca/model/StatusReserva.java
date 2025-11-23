package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_status_reserva")
public class StatusReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_status")
    private Integer id;

    @Column(name = "nome_status")
    private String nome;

    // ======================
    // GETTERS E SETTERS
    // ======================

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
}
