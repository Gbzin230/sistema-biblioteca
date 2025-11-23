package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_reserva") // sempre deixe minúsculo igual no banco
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_reserva")
    private Long id;

    @Column(name = "dt_inicio_reserva")
    private LocalDateTime dtInicioReserva;

    @Column(name = "dt_fim_reserva")
    private LocalDateTime dtFimReserva;

    @ManyToOne
    @JoinColumn(name = "cod_status_reserva") // FK -> tb_status_reserva(cod_status)
    private StatusReserva status;

    @ManyToOne
    @JoinColumn(name = "cod_username")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "cod_livro")
    private Livro livro;

    public Reserva() {
        this.dtInicioReserva = LocalDateTime.now();
    }

    public Reserva(Usuario usuario, Livro livro, StatusReserva status) {
        this();
        this.usuario = usuario;
        this.livro = livro;
        this.status = status;
    }

    // ======================
    // GETTERS E SETTERS
    // ======================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDtInicioReserva() {
        return dtInicioReserva;
    }

    public void setDtInicioReserva(LocalDateTime dtInicioReserva) {
        this.dtInicioReserva = dtInicioReserva;
    }

    public LocalDateTime getDtFimReserva() {
        return dtFimReserva;
    }

    public void setDtFimReserva(LocalDateTime dtFimReserva) {
        this.dtFimReserva = dtFimReserva;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }
}
