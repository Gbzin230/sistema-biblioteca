package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_RESERVA")
public class Reserva {

    public enum ReservaStatus {
        ATIVA,
        CONFIRMADA,
        CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_reserva")
    private Long id;

    @Column(name = "dt_inicio_reserva")
    private LocalDateTime dtInicioReserva;

    @Column(name = "dt_fim_reserva")
    private LocalDateTime dtFimReserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "cod_status_reserva")
    private ReservaStatus status;

    @ManyToOne
    @JoinColumn(name = "cod_username") // ← Nome correto do banco
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "cod_livro")
    private Livro livro;

    public Reserva() {
        this.dtInicioReserva = LocalDateTime.now();
        this.status = ReservaStatus.ATIVA;
    }

    public Reserva(Usuario usuario, Livro livro) {
        this();
        this.usuario = usuario;
        this.livro = livro;
    }

    public Emprestimo confirmar() {
        if (this.status == ReservaStatus.ATIVA && livro != null) {
            this.status = ReservaStatus.CONFIRMADA;
            return new Emprestimo(this.usuario, this.livro);
        }
        return null;
    }

    public void cancelar() {
        this.status = ReservaStatus.CANCELADA;
    }

    // Getters e Setters =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDtInicioReserva() { return dtInicioReserva; }
    public void setDtInicioReserva(LocalDateTime dtInicioReserva) { this.dtInicioReserva = dtInicioReserva; }

    public LocalDateTime getDtFimReserva() { return dtFimReserva; }
    public void setDtFimReserva(LocalDateTime dtFimReserva) { this.dtFimReserva = dtFimReserva; }

    public ReservaStatus getStatus() { return status; }
    public void setStatus(ReservaStatus status) { this.status = status; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
}
