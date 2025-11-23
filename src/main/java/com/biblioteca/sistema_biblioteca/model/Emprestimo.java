package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_emprestimo")
    private Long id;

    @Column(name = "dt_inicio")
    private LocalDateTime dtInicio;

    @Column(name = "dt_fim")
    private LocalDateTime dtFim;

    @Column(name = "num_renovacoes")
    private Integer numRenovacoes;

    @Column(name = "num_pagina_atual")
    private Integer paginaAtual;

    // STATUS NÃO PODE SER EDITADO VIA setNome
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cod_status", nullable = false)
    private StatusEmprestimo status;

    @ManyToOne
    @JoinColumn(name = "cod_username", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "cod_livro", nullable = false)
    private Livro livro;

    // =============================
    // CONSTRUTORES
    // =============================
    public Emprestimo() {
        this.numRenovacoes = 0;
        this.paginaAtual = 0;
        this.dtInicio = LocalDateTime.now();
        this.dtFim = dtInicio.plusDays(7);
    }

    public Emprestimo(Usuario usuario, Livro livro) {
        this();
        this.usuario = usuario;
        this.livro = livro;
    }

    // =============================
    // REGRAS DE NEGÓCIO
    // =============================
    public boolean renovar(StatusEmprestimo statusAtivo) {
        if (!this.status.getNome().equalsIgnoreCase("ATIVO"))
            return false;

        if (this.numRenovacoes < 2) {
            this.dtFim = this.dtFim.plusDays(14);
            this.numRenovacoes++;
            this.status = statusAtivo;
            return true;
        }
        return false;
    }

    public void encerrar(StatusEmprestimo statusFinalizado) {
        this.status = statusFinalizado;
    }

    public void atualizarStatusAtraso(StatusEmprestimo statusAtrasado) {
        if (this.status.getNome().equalsIgnoreCase("FINALIZADO"))
            return;

        if (LocalDateTime.now().isAfter(this.dtFim)) {
            this.status = statusAtrasado;
        }
    }

    // =============================
    // GETTERS E SETTERS
    // =============================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDtInicio() { return dtInicio; }
    public void setDtInicio(LocalDateTime dtInicio) { this.dtInicio = dtInicio; }

    public LocalDateTime getDtFim() { return dtFim; }
    public void setDtFim(LocalDateTime dtFim) { this.dtFim = dtFim; }

    public Integer getNumRenovacoes() { return numRenovacoes; }
    public void setNumRenovacoes(Integer numRenovacoes) { this.numRenovacoes = numRenovacoes; }

    public Integer getPaginaAtual() { return paginaAtual; }
    public void setPaginaAtual(Integer paginaAtual) { this.paginaAtual = paginaAtual; }

    public StatusEmprestimo getStatus() { return status; }
    public void setStatus(StatusEmprestimo status) { this.status = status; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
}
