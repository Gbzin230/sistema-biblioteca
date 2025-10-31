package com.biblioteca.sistema_biblioteca.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String autor;
    private String editora;
    private String tema;

    @ElementCollection
    private List<String> tags;

    private Integer anoLancamento;
    private Boolean flagAtivo;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 2000)
    private String sinopse;

    public enum Status {
        DISPONIVEL,
        EMPRESTADO,
        RESERVADO,
        INATIVO

    }

    // comportamentos do domínio (regras de negócio)

    public Status consultarStatus() {
        return this.status;
    }

    public void alterarStatus(Status novoStatus) {
        if (Boolean.TRUE.equals(this.flagAtivo)) {
            this.status = novoStatus;
        } else {
            throw new IllegalStateException("Não é possível alterar o status, pois o livro está inativo");
        }
    }

    public boolean isDisponivel() {
        return this.status == Status.DISPONIVEL && Boolean.TRUE.equals(this.flagAtivo);
    }

    // Getters e Setters
    // Getters e Setters manuais
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(Integer anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public Boolean getFlagAtivo() {
        return flagAtivo;
    }

    public void setFlagAtivo(Boolean flagAtivo) {
        this.flagAtivo = flagAtivo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

}