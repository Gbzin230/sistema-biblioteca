package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Reserva {

    public enum ReservaStatus {
        ATIVA,
        CONFIRMADA,
        CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dtSolicitacao;
    private Integer posicaoFila;

    private ReservaStatus status;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "livro_id")
    private Livro livro;

    public Reserva() {
        this.dtSolicitacao = LocalDate.now();
        this.status = ReservaStatus.ATIVA;
    }

    // ======== Métodos de Negócio ========

    public Reserva(Usuario usuario, Livro livro) {
        this();
        this.usuario = usuario;
        this.livro = livro;
    }

    public Emprestimo confirmar() {
        if (this.status == ReservaStatus.ATIVA && livro != null && livro.isDisponivel()) {
            this.status = ReservaStatus.CONFIRMADA;
            livro.alterarStatus(Livro.Status.EMPRESTADO);

            Emprestimo emprestimo = new Emprestimo(this.usuario, this.livro);
            return emprestimo;
        }
        return null;
    }

    public void cancelar() {
        this.status = ReservaStatus.CANCELADA;
    }

    public void atualizarPosicaoFila(Integer posicaoFila) {
        this.posicaoFila = posicaoFila;
    }

    // ======== Getters e Setters ========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDtSolicitacao() {
        return dtSolicitacao;
    }

    public void setDtSolicitacao(LocalDate dtSolicitacao) {
        this.dtSolicitacao = dtSolicitacao;
    }

    public Integer getPosicaoFila() {
        return posicaoFila;
    }

    public void setPosicaoFila(Integer posicaoFila) {
        this.posicaoFila = posicaoFila;
    }

    public ReservaStatus getStatus() {
        return status;
    }

    public void setStatus(ReservaStatus status) {
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
