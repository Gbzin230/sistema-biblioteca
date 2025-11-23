package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_status_usuario")
public class StatusUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_status")
    private Integer id;

    @Column(name = "nome_status", nullable = false, length = 50)
    private String nomeStatus;

    // CONSTRUTORES
    public StatusUsuario() {}

    public StatusUsuario(String nomeStatus) {
        this.nomeStatus = nomeStatus;
    }

    // GETTERS / SETTERS
    public Integer getId() {
        return id;
    }

    public String getNomeStatus() {
        return nomeStatus;
    }

    public void setNomeStatus(String nomeStatus) {
        this.nomeStatus = nomeStatus;
    }
}
