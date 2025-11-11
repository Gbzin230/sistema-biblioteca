package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dtInicio;
    private LocalDate dtPrevistaDevolucao;
    private Integer numRenovacoes;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    public enum Status {
        ATIVO,
        FINALIZADO,
        ATRASADO
    }

    // ======== Construtores ========

    public Emprestimo() {
        this.status = Status.ATIVO;
        this.numRenovacoes = 0;
        this.dtInicio = LocalDate.now();
        this.dtPrevistaDevolucao = dtInicio.plusDays(7); // prazo padrão 7 dias
    }

    public Emprestimo(Usuario usuario, Livro livro) {
        this();
        this.usuario = usuario;
        this.livro = livro;
    }

    // ======== Métodos de Negócio ========

    public boolean renovar() {
        if (this.status == Status.ATIVO && this.numRenovacoes < 2) {
            this.dtPrevistaDevolucao = this.dtPrevistaDevolucao.plusDays(14);
            this.numRenovacoes++;
            return true;
        }
        return false;
    }

    public void encerrar() {
        this.status = Status.FINALIZADO;
    }

    public Status verificarStatus() {
        if (this.status == Status.FINALIZADO) {
            return Status.FINALIZADO;
        }
        if (LocalDate.now().isAfter(this.dtPrevistaDevolucao)) {
            this.status = Status.ATRASADO;
        }
        return this.status;
    }

    // ======== Getters e Setters ========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDtInicio() {
        return dtInicio;
    }

    public void setDtInicio(LocalDate dtInicio) {
        this.dtInicio = dtInicio;
    }

    public LocalDate getDtPrevistaDevolucao() {
        return dtPrevistaDevolucao;
    }

    public void setDtPrevistaDevolucao(LocalDate dtPrevistaDevolucao) {
        this.dtPrevistaDevolucao = dtPrevistaDevolucao;
    }

    public Integer getNumRenovacoes() {
        return numRenovacoes;
    }

    public void setNumRenovacoes(Integer numRenovacoes) {
        this.numRenovacoes = numRenovacoes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

