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
    private String status;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToOne
    @JoinColumn(name = "livro_id")
    private Livro livro;

    public Emprestimo() {
        this.status = "ATIVO";
        this.numRenovacoes = 0;
        this.dtInicio = LocalDate.now();
        this.dtPrevistaDevolucao = dtInicio.plusDays(14); // Ex: prazo padrão 14 dias
    }

    // ======== Métodos de Negócio ========

    public Emprestimo(Usuario usuario, Livro livro) {
        this();
        this.usuario = usuario;
        this.livro = livro;
    }

    public boolean renovar() {
        if ("ATIVO".equals(this.status) && this.numRenovacoes < 2) {
            this.dtPrevistaDevolucao = this.dtPrevistaDevolucao.plusDays(14);
            this.numRenovacoes++;
            return true;
        }
        return false;
    }

    public void encerrar() {
        this.status = "ENCERRADO";
    }

    public String verificarStatus() {
        if ("ENCERRADO".equals(this.status)) {
            return "Encerrado";
        }
        if (LocalDate.now().isAfter(this.dtPrevistaDevolucao)) {
            this.status = "ATRASADO";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
