package com.biblioteca.sistema_biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dtSolicitacao;
    private Integer posicaoFila;
    private String status;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToOne
    @JoinColumn(name = "livro_id")
    private Livro livro;

    public Reserva() {
        this.dtSolicitacao = LocalDate.now();
        this.status = "ATIVA";
    }

    // ======== Métodos de Negócio ========

    public Emprestimo confirmar() {
        if ("ATIVA".equals(this.status) && "DISPONIVEL".equals(livro.getStatus())) {
            this.status = "CONFIRMADA";
            livro.setStatus("ALUGADO");

            Emprestimo emprestimo = new Emprestimo();
            emprestimo.setLivro(this.livro);
            emprestimo.setUsuario(this.usuario);
            return emprestimo;
        }
        return null;
    }

    public void cancelar() {
        this.status = "CANCELADA";
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


